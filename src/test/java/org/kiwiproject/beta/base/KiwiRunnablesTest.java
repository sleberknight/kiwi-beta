package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchException;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.fourth;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.third;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.CatchingRunnable;
import org.kiwiproject.beta.base.KiwiRunnables.RunResult;
import org.kiwiproject.beta.base.KiwiRunnables.ThrowingRunnable;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("KiwiRunnables")
class KiwiRunnablesTest {

    @DisplayName("ThrowingRunnable")
    @Nested
    class ThrowingRunnableTests {

        @Test
        void shouldCreateFromRunnable() throws Exception {
            var called = new AtomicBoolean();
            Runnable r = () -> called.set(true);

            var tr = ThrowingRunnable.of(r);
            tr.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldCreateFromRunnableThatThrowsException() {
            Runnable r = () -> {
                throw new RuntimeException("oops");
            };

            var tr = ThrowingRunnable.of(r);

            assertThatThrownBy(tr::run)
                    .isExactlyInstanceOf(RuntimeException.class)
                    .hasMessage("oops");
        }

        @Test
        void shouldCreateFromCatchingRunnable() throws Exception {
            var called = new AtomicBoolean();
            CatchingRunnable cr = () -> called.set(true);

            var tr = ThrowingRunnable.of(cr);
            tr.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldCreateFromCatchingRunnableThatThrowsException() {
            var called = new AtomicBoolean();
            CatchingRunnable cr = () -> {
                called.set(true);
                throw new RuntimeException("oops");
            };

            var tr = ThrowingRunnable.of(cr);

            assertThatCode(tr::run).doesNotThrowAnyException();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToRunnable() {
            var called = new AtomicBoolean();
            ThrowingRunnable tr = () -> called.set(true);

            var r = tr.toRunnable();
            r.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToRunnable_AndWrapExceptions() {
            var tr = new ThrowingRunnable() {
                @Override
                public void run() throws Exception {
                    throw new IOException("I/O oops");
                }
            };

            var r = tr.toRunnable();
            var thrown = catchException(r::run);

            assertThat(thrown)
                    .isExactlyInstanceOf(KiwiRunnables.WrappedException.class)
                    .hasCauseExactlyInstanceOf(IOException.class)
                    .hasMessage("Contains Exception thrown by a wrapped ThrowingRunnable");

            assertThat(thrown.getCause()).hasMessage("I/O oops");
        }

        @Test
        void shouldConvertToCatchingRunnable() {
            var called = new AtomicBoolean();
            ThrowingRunnable tr = () -> called.set(true);

            var cr = tr.toCatchingRunnable();
            cr.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToCatchingRunnable_AndWrapExceptions() {
            var tr = new ThrowingRunnable() {
                @Override
                public void run() throws Exception {
                    throw new IOException("I/O oops");
                }
            };

            var cr = tr.toCatchingRunnable();

            assertThatCode(cr::run).doesNotThrowAnyException();
        }

        @Test
        void shouldConvertToCatchingRunnable2() {
            var called = new AtomicBoolean();
            ThrowingRunnable tr = () -> called.set(true);

            var cr2 = tr.toCatchingRunnable2();
            cr2.run();

            assertThat(called).isTrue();
        }

        @Test
        void shouldConvertToCatchingRunnable2_AndWrapExceptions() {
            var tr = new ThrowingRunnable() {
                @Override
                public void run() throws Exception {
                    throw new IOException("I/O oops");
                }
            };

            var cr2 = tr.toCatchingRunnable2();

            assertThatCode(cr2::run).doesNotThrowAnyException();
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

    @Nested
    class RunResults {

        @Test
        void shouldCreateForSuccess() {
            var result = RunResult.ofSuccess();

            assertThat(result.success()).isTrue();
            assertThat(result.hasError()).isFalse();
            assertThat(result.error()).isNull();
        }

        @Test
        void shouldCreateForError() {
            var message = "I/O failed - disk full";
            var ioError = new IOException(message);
            var result = RunResult.ofError(ioError);

            assertThat(result.success()).isFalse();
            assertThat(result.hasError()).isTrue();
            assertThat(result.error()).isSameAs(ioError);
        }

        @Test
        void shouldNotAllowNullExceptionWhenCreatingForError() {
            assertThatIllegalArgumentException().isThrownBy(() -> RunResult.ofError(null));
        }
    }

    @Nested
    class RunAll {

        @Test
        void shouldReturnAllResults() {
            var called1 = new AtomicBoolean();
            var called2 = new AtomicBoolean();
            var called3 = new AtomicBoolean();
            var called4 = new AtomicBoolean();

            var results = KiwiRunnables.runAll(
                    () -> called1.set(true),
                    () -> {
                        called2.set(true);
                        throw new IOException("I/O error - disk full");
                    },
                    () -> called3.set(true),
                    () -> {
                        called4.set(true);
                        throw new IOException("I/O error - access denied");
                    });

            assertThat(first(results).success()).isTrue();

            assertThat(second(results).hasError()).isTrue();
            assertThat(second(results).error())
                    .isExactlyInstanceOf(IOException.class)
                    .hasMessage("I/O error - disk full");

            assertThat(third(results).success()).isTrue();

            assertThat(fourth(results).hasError()).isTrue();
            assertThat(fourth(results).error())
                    .isExactlyInstanceOf(IOException.class)
                    .hasMessage("I/O error - access denied");

            assertThat(called1).isTrue();
            assertThat(called2).isTrue();
            assertThat(called3).isTrue();
            assertThat(called4).isTrue();
        }
    }

    @Nested
    class Run {

        @Test
        void shouldReturnSuccessfulResults() {
            var called = new AtomicBoolean();

            var result = KiwiRunnables.run(() -> called.set(true));

            assertThat(result.success()).isTrue();
            assertThat(called).isTrue();
        }

        @Test
        void shouldReturnErrorResults() {
            var called = new AtomicBoolean();

            var result = KiwiRunnables.run(() -> {
                called.set(true);
                throw new SocketTimeoutException("Timed out after 10 seconds");
            });

            assertThat(result.hasError()).isTrue();
            assertThat(result.error())
                    .isExactlyInstanceOf(SocketTimeoutException.class)
                    .hasMessage("Timed out after 10 seconds");
            assertThat(called).isTrue();
        }
    }
}
