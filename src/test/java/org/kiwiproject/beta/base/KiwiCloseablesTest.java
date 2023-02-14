package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.kiwiproject.collect.KiwiLists.fifth;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.fourth;
import static org.kiwiproject.collect.KiwiLists.nth;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.kiwiproject.beta.base.KiwiCloseables.CloseDescriptor;
import org.kiwiproject.beta.base.KiwiCloseables.CloseResult;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;

import java.io.Closeable;
import java.io.IOException;

@DisplayName("KiwiCloseables")
class KiwiCloseablesTest {

    static class NiceCloseable implements Closeable {
        boolean closed;

        @SuppressWarnings("RedundantThrows")
        @Override
        public void close() throws IOException {
            closed = true;
        }
    }

    static class BadCloseable implements Closeable {
        boolean closeAttempted;

        @Override
        public void close() throws IOException {
            closeAttempted = true;
            throw new IOException("Error closing");
        }
    }

    static class NiceStoppable {
        boolean stopped;

        @SuppressWarnings("unused")
        public void stop() {
            stopped = true;
        }
    }

    static class BadStoppable {
        boolean stopAttempted;

        @SuppressWarnings("unused")
        public void stop() {
            stopAttempted = true;
            throw new RuntimeException("Unable to stop!");
        }
    }

    static class SecretStoppable {
        boolean closed;

        @SuppressWarnings("unused")
        private void stop() {
            closed = true;
        }
    }

    static class ThingCanBeClosed {
        boolean closed;

        @SuppressWarnings({"unused", "RedundantThrows"})
        public void close() throws IOException {
            closed = true;
        }
    }

    @Nested
    class CloseDescriptors {

        @Test
        void shouldAllowNullObjects() {
            var descriptor = CloseDescriptor.of(null);
            assertThat(descriptor.object()).isNull();
        }

        @Test
        void shouldProvideDefaultCloseMethodName() {
            var descriptor = CloseDescriptor.of(new NiceCloseable());
            assertThat(descriptor.closeMethodName()).isEqualTo("close");
        }

        @Test
        void shouldAllowCustomCloseMethodName() {
            var descriptor = CloseDescriptor.of(new NiceStoppable(), "stop");
            assertThat(descriptor.closeMethodName()).isEqualTo("stop");
        }

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldRequireCloseMethodName(String name) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> CloseDescriptor.of(null, name))
                    .withMessage("closeMethodName must not be blank");
        }
    }

    @Nested
    class CloseAllQuietly {

        @Test
        void shouldNotAllowNullArgument() {
            // No one should ever do this, but just in case...
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCloseables.closeAllQuietly((Object[]) null))
                    .withMessage("don't cast varargs to null!");
        }

        @Test
        void shouldIgnoreNullObjects() {
            assertThatCode(() -> KiwiCloseables.closeAllQuietly(null, null)).doesNotThrowAnyException();
        }

        @SuppressWarnings("resource")
        @Test
        void shouldCloseCloseables() {
            var c1 = new BadCloseable();
            var c2 = new NiceCloseable();
            var c3 = new NiceCloseable();
            var c4 = new BadCloseable();

            assertThatCode(() -> KiwiCloseables.closeAllQuietly(c1, c2, c3, c4)).doesNotThrowAnyException();

            assertThat(c1.closeAttempted).isTrue();
            assertThat(c2.closed).isTrue();
            assertThat(c3.closed).isTrue();
            assertThat(c4.closeAttempted).isTrue();
        }

        @Test
        void shouldCloseCloseableDescriptors() {
            var s1 = new NiceStoppable();
            var s2 = new BadStoppable();
            var s3 = new BadStoppable();
            var c1 = new NiceCloseable();

            var d1 = CloseDescriptor.of(s1, "stop");
            var d2 = CloseDescriptor.of(s2, "stop");
            var d3 = CloseDescriptor.of(s3, "stop");
            var d4 = CloseDescriptor.of(c1);

            assertThatCode(() -> KiwiCloseables.closeAllQuietly(d1, d2, d3, d4)).doesNotThrowAnyException();

            assertThat(s1.stopped).isTrue();
            assertThat(s2.stopAttempted).isTrue();
            assertThat(s3.stopAttempted).isTrue();
            assertThat(c1.closed).isTrue();
        }

        @SuppressWarnings("resource")
        @Test
        void shouldCloseAnything() {
            var o1 = new BadCloseable();
            var o2 = new NiceStoppable();
            var o3 = new NiceCloseable();
            var o4 = new BadStoppable();
            var o5 = new ThingCanBeClosed();

            var o2Descriptor = CloseDescriptor.of(o2, "stop");
            var o4Descriptor = CloseDescriptor.of(o4, "stop");
            var o5Descriptor = CloseDescriptor.of(o5, "close");

            assertThatCode(
                    () -> KiwiCloseables.closeAllQuietly(
                            null, o1, o2Descriptor, null, o3, null, null, o4Descriptor, o5Descriptor, null, null))
                    .doesNotThrowAnyException();

            assertThat(o1.closeAttempted).isTrue();
            assertThat(o2.stopped).isTrue();
            assertThat(o3.closed).isTrue();
            assertThat(o4.stopAttempted).isTrue();
            assertThat(o5.closed).isTrue();
        }
    }

    @Nested
    class CloseQuietlyWithCloseDescriptor {

        @Test
        void shouldRequireNonNullDescriptor() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCloseables.closeQuietly((CloseDescriptor) null));
        }

        @Test
        void shouldIgnoreNullObjects() {
            var descriptor = CloseDescriptor.of(null, "close");
            assertThatCode(() -> KiwiCloseables.closeQuietly(descriptor)).doesNotThrowAnyException();
        }

        @Test
        void shouldCloseNiceCloseables() {
            var closeable = new NiceCloseable();
            var descriptor = CloseDescriptor.of(closeable);

            KiwiCloseables.closeQuietly(descriptor);

            assertThat(closeable.closed).isTrue();
        }

        @Test
        void shouldCloseCloseablesThatThrowIOException() {
            var closeable = new BadCloseable();
            var descriptor = CloseDescriptor.of(closeable);

            assertThatCode(() -> KiwiCloseables.closeQuietly(descriptor)).doesNotThrowAnyException();

            assertThat(closeable.closeAttempted).isTrue();
        }

        @Test
        void shouldCloseObjectsHavingDefaultCloseMethodName() {
            var o = new ThingCanBeClosed();
            var descriptor = CloseDescriptor.of(o);

            KiwiCloseables.closeQuietly(descriptor);

            assertThat(o.closed).isTrue();
        }

        @Test
        void shouldCloseObjectsHavingCustomCloseMethodName() {
            var o = new NiceStoppable();
            var descriptor = CloseDescriptor.of(o, "stop");

            KiwiCloseables.closeQuietly(descriptor);

            assertThat(o.stopped).isTrue();
        }
    }

    @Nested
    class CloseQuietlyWithCloseable {

        @Test
        void shouldIgnoreNullObjects() {
            assertThatCode(() -> KiwiCloseables.closeQuietly((Closeable) null)).doesNotThrowAnyException();
        }

        @Test
        void shouldClose() {
            var closeable = new NiceCloseable();

            KiwiCloseables.closeQuietly(closeable);

            assertThat(closeable.closed).isTrue();
        }

        @SuppressWarnings("resource")
        @Test
        void shouldCloseCloseablesThatThrowIOException() {
            var closeable = new BadCloseable();

            assertThatCode(() -> KiwiCloseables.closeQuietly(closeable)).doesNotThrowAnyException();

            assertThat(closeable.closeAttempted).isTrue();
        }

        @Test
        void shouldThrowIllegalState_WhenGivenInvalidCloseDescriptor() {
            var descriptor = mock(CloseDescriptor.class);
            when(descriptor.closeMethodName()).thenReturn(null);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiCloseables.closeQuietly(descriptor))
                    .withMessage("invalid CloseDescriptor! closeMethodName is blank!");
        }
    }

    @Nested
    class CloseResults {

        @Test
        void shouldCreateForClosed() {
            var result = CloseResult.ofClosed();
            assertThat(result.closed()).isTrue();
            assertThat(result.wasNull()).isFalse();
            assertThat(result.closedOrWasNull()).isTrue();
            assertThat(result.hasError()).isFalse();
            assertThat(result.error()).isNull();
        }

        @Test
        void shouldCreateForNullArgument() {
            var result = CloseResult.ofNull();
            assertThat(result.closed()).isFalse();
            assertThat(result.wasNull()).isTrue();
            assertThat(result.closedOrWasNull()).isTrue();
            assertThat(result.hasError()).isFalse();
            assertThat(result.error()).isNull();
        }

        @Test
        void shouldCreateForError() {
            var ioException = new IOException("Unable to close");
            var result = CloseResult.ofError(ioException);
            assertThat(result.closed()).isFalse();
            assertThat(result.wasNull()).isFalse();
            assertThat(result.closedOrWasNull()).isFalse();
            assertThat(result.hasError()).isTrue();
            assertThat(result.error()).isSameAs(ioException);
        }
    }

    @Nested
    class CloseAll {

        @Test
        void shouldNotAllowNullArgument() {
            // No one should ever do this, but just in case...
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCloseables.closeAll((Object[]) null))
                    .withMessage("don't cast varargs to null!");
        }

        @Test
        void shouldIgnoreNullObjects() {
            assertThatCode(() -> KiwiCloseables.closeAll(null, null)).doesNotThrowAnyException();
        }

        @Test
        void shouldCloseCloseables() {
            var c1 = new BadCloseable();
            var c2 = new NiceCloseable();
            var c3 = new NiceCloseable();
            var c4 = new BadCloseable();

            var closeResults = KiwiCloseables.closeAll(c1, c2, c3, c4);

            assertThat(first(closeResults).hasError()).isTrue();
            assertThat(second(closeResults).closed()).isTrue();
            assertThat(third(closeResults).closed()).isTrue();
            assertThat(fourth(closeResults).hasError()).isTrue();

            assertThat(c1.closeAttempted).isTrue();
            assertThat(c2.closed).isTrue();
            assertThat(c3.closed).isTrue();
            assertThat(c4.closeAttempted).isTrue();
        }

        @Test
        void shouldCloseCloseableDescriptors() {
            var s1 = new NiceStoppable();
            var s2 = new BadStoppable();
            var s3 = new BadStoppable();
            var c1 = new NiceCloseable();

            var d1 = CloseDescriptor.of(s1, "stop");
            var d2 = CloseDescriptor.of(s2, "stop");
            var d3 = CloseDescriptor.of(s3, "stop");
            var d4 = CloseDescriptor.of(c1);

            var closeResults = KiwiCloseables.closeAll(d1, d2, d3, d4);

            assertThat(first(closeResults).closed()).isTrue();
            assertThat(second(closeResults).hasError()).isTrue();
            assertThat(third(closeResults).hasError()).isTrue();
            assertThat(fourth(closeResults).closed()).isTrue();

            assertThat(s1.stopped).isTrue();
            assertThat(s2.stopAttempted).isTrue();
            assertThat(s3.stopAttempted).isTrue();
            assertThat(c1.closed).isTrue();
        }

        @Test
        void shouldCloseAnything() {
            var o1 = new BadCloseable();
            var o2 = new NiceStoppable();
            var o3 = new NiceCloseable();
            var o4 = new BadStoppable();
            var o5 = new ThingCanBeClosed();

            var o2Descriptor = CloseDescriptor.of(o2, "stop");
            var o4Descriptor = CloseDescriptor.of(o4, "stop");
            var o5Descriptor = CloseDescriptor.of(o5, "close");

            var closeResults = KiwiCloseables.closeAll(
                    null, o1, o2Descriptor, null, o3, o4Descriptor, o5Descriptor, null);

            assertThat(o1.closeAttempted).isTrue();
            assertThat(o2.stopped).isTrue();
            assertThat(o3.closed).isTrue();
            assertThat(o4.stopAttempted).isTrue();
            assertThat(o5.closed).isTrue();

            assertThat(first(closeResults).wasNull()).isTrue();
            assertThat(second(closeResults).hasError()).isTrue();
            assertThat(third(closeResults).closed()).isTrue();
            assertThat(fourth(closeResults).wasNull()).isTrue();
            assertThat(fifth(closeResults).closed()).isTrue();
            assertThat(nth(closeResults, 6).hasError()).isTrue();
            assertThat(nth(closeResults, 7).closed()).isTrue();
            assertThat(nth(closeResults, 8).wasNull()).isTrue();
        }
    }

    @Nested
    class CloseWithCloseDescriptor {

        @Test
        void shouldRequireNonNullDescriptor() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiCloseables.close((CloseDescriptor) null));
        }

        @Test
        void shouldIgnoreNullObjects() {
            var descriptor = CloseDescriptor.of(null, "close");
            var closeResult = KiwiCloseables.close(descriptor);

            assertThat(closeResult.wasNull()).isTrue();
        }

        @Test
        void shouldCloseNiceCloseables() {
            var closeable = new NiceCloseable();
            var descriptor = CloseDescriptor.of(closeable);

            var closeResult = KiwiCloseables.close(descriptor);

            assertThat(closeable.closed).isTrue();
            assertThat(closeResult.closed()).isTrue();
        }

        @Test
        void shouldCloseWhenCloseMethodIsPrivate() {
            var stoppable = new SecretStoppable();
            var descriptor = CloseDescriptor.of(stoppable, "stop");

            var closeResult = KiwiCloseables.close(descriptor);

            assertThat(stoppable.closed).isTrue();
            assertThat(closeResult.closed()).isTrue();
        }

        @Test
        void shouldCloseCloseablesThatThrowIOException() {
            var closeable = new BadCloseable();
            var descriptor = CloseDescriptor.of(closeable);

            var closeResult = KiwiCloseables.close(descriptor);

            assertThat(closeable.closeAttempted).isTrue();
            assertThat(closeResult.hasError()).isTrue();
            assertThat(closeResult.error()).isInstanceOf(IOException.class);
        }

        @Test
        void shouldCloseObjectsHavingDefaultCloseMethodName() {
            var o = new ThingCanBeClosed();
            var descriptor = CloseDescriptor.of(o);

            var closeResult = KiwiCloseables.close(descriptor);

            assertThat(o.closed).isTrue();
            assertThat(closeResult.closed()).isTrue();
        }

        @Test
        void shouldCloseObjectsHavingCustomCloseMethodName() {
            var o = new NiceStoppable();
            var descriptor = CloseDescriptor.of(o, "stop");

            var closeResult = KiwiCloseables.close(descriptor);

            assertThat(o.stopped).isTrue();
            assertThat(closeResult.closed()).isTrue();
        }
    }

    @Nested
    class CloseWithCloseable {

        @Test
        void shouldIgnoreNullObjects() {
            var closeResult = KiwiCloseables.close((Closeable) null);

            assertThat(closeResult.wasNull()).isTrue();
        }

        @Test
        void shouldClose() {
            var closeable = new NiceCloseable();

            var closeResult = KiwiCloseables.close(closeable);

            assertThat(closeable.closed).isTrue();
            assertThat(closeResult.closed()).isTrue();
        }

        @Test
        void shouldCloseCloseablesThatThrowIOException() {
            var closeable = new BadCloseable();

            var closeResult = KiwiCloseables.close(closeable);

            assertThat(closeable.closeAttempted).isTrue();
            assertThat(closeResult.hasError()).isTrue();
            assertThat(closeResult.error()).isInstanceOf(IOException.class);
        }

        @Test
        void shouldThrowIllegalState_WhenGivenInvalidCloseDescriptor() {
            var descriptor = mock(CloseDescriptor.class);
            when(descriptor.closeMethodName()).thenReturn(null);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiCloseables.close(descriptor))
                    .withMessage("invalid CloseDescriptor! closeMethodName is blank!");
        }
    }
}
