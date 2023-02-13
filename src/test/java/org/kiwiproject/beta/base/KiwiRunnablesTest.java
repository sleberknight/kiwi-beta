package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.CatchingRunnable;
import org.kiwiproject.beta.base.KiwiRunnables.ThrowingRunnable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("KiwiRunnables")
class KiwiRunnablesTest {

    @Nested
    class ThrowingRunnableTests {

        @Test
        void shouldCreateFromNiceRunnable() throws Exception {
            var called = new AtomicBoolean();
            Runnable r = () -> called.set(true);

            var tr = ThrowingRunnable.of(r);
            tr.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldCreateFromThrowingRunnable() {
            Runnable r = () -> {
                throw new RuntimeException("oops");
            };

            var tr = ThrowingRunnable.of(r);

            assertThatThrownBy(() -> tr.run())
                    .isExactlyInstanceOf(RuntimeException.class)
                    .hasMessage("oops");
        }

        @Test
        void shouldCreateFromNiceCatchingRunnable() throws Exception {
            var called = new AtomicBoolean();
            CatchingRunnable cr = () -> called.set(true);

            var tr = ThrowingRunnable.of(cr);
            tr.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldCreateFromThrowingCatchingRunnable() throws Exception {
            var called = new AtomicBoolean();
            CatchingRunnable cr = () -> {
                called.set(true);
                throw new RuntimeException("oops");
            };

            var tr = ThrowingRunnable.of(cr);
            tr.run();

            assertThatCode(() -> tr.run()).doesNotThrowAnyException();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToRunnableThatIsNice() {
            var called = new AtomicBoolean();
            ThrowingRunnable tr = () -> called.set(true);

            var r = tr.toRunnable();
            r.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToRunnableThatThrows() {
            var tr = new ThrowingRunnable() {
                @Override
                public void run() throws Exception {
                    throw new IOException("I/O oops");
                }
            };

            var r = tr.toRunnable();
            var thrown = catchException(() -> r.run());

            assertThat(thrown)
                .isExactlyInstanceOf(RuntimeException.class)
                .hasCauseExactlyInstanceOf(IOException.class)
                .hasMessage("Re-throwing Exception thrown by wrapped ThrowingRunnable");

            assertThat(thrown.getCause()).hasMessage("I/O oops");
        }

        @Test
        void shouldConvertToCatchingRunnableThatIsNice() {
            var called = new AtomicBoolean();
            ThrowingRunnable tr = () -> called.set(true);

            var cr = tr.toCatchingRunnable();
            cr.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToCatchingRunnableThatThrows() {
            var tr = new ThrowingRunnable() {
                @Override
                public void run() throws Exception {
                    throw new IOException("I/O oops");
                }
            };

            var cr = tr.toCatchingRunnable();

            assertThatCode(() -> cr.run()).doesNotThrowAnyException();
        }

        @Test
        void shouldConvertToCatchingRunnable2ThatIsNice() {
            var called = new AtomicBoolean();
            ThrowingRunnable tr = () -> called.set(true);

            var cr2 = tr.toCatchingRunnable2();
            cr2.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToCatchingRunnable2ThatThrows() {
            var tr = new ThrowingRunnable() {
                @Override
                public void run() throws Exception {
                    throw new IOException("I/O oops");
                }
            };

            var cr2 = tr.toCatchingRunnable2();

            assertThatCode(() -> cr2.run()).doesNotThrowAnyException();
        }
    }

    @Nested
    class RunAllQuietly {

        @Test
        void shouldCallRunnables() {
            var called1 = new AtomicBoolean();
            var called2 = new AtomicBoolean();
            var called3 = new AtomicBoolean();

            KiwiRunnables.runAllQuietly(
                    () -> called1.set(true),
                    () -> called2.set(true),
                    () -> called3.set(true));

                    assertThat(called1).isTrue();
            assertThat(called2).isTrue();
            assertThat(called3).isTrue();
        }

        @Test
        void shouldSuppressExceptions() {
            var called1 = new AtomicBoolean();
            var called2 = new AtomicBoolean();
            var called3 = new AtomicBoolean();

            KiwiRunnables.runAllQuietly(
                    () -> {
                        called1.set(true);
                        throw new RuntimeException("I failed");
                    },
                    () -> called2.set(true),
                    () -> {
                        called3.set(true);
                        throw new IOException("I/O failed");
                    });

                    assertThat(called1).isTrue();
            assertThat(called2).isTrue();
            assertThat(called3).isTrue();
        }
    }

    @Nested
    class RunQuietly {

        @Test
        void shouldCallRunnable() {
            var called = new AtomicBoolean();

            KiwiRunnables.runQuietly(() -> called.set(true));

            assertThat(called).isTrue();
        }

        @Test
        void shouldSuppressExceptions() {
            var called = new AtomicBoolean();

            KiwiRunnables.runQuietly(() -> {
                called.set(true);
                throw new Exception("oops");
            });

            assertThat(called).isTrue();
        }
    }

}
