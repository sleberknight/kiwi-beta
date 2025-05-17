package org.kiwiproject.beta.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.beta.concurrent.KiwiFutures.FutureState;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class KiwiFuturesTest {

    @AfterEach
    void tearDown() {
        KiwiFutures.resetInterruptedExceptionHandler();
    }

    @Nested
    class ResultNow {

        @Test
        void shouldThrowIllegalArgument_WhenGivenNullFuture() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiFutures.resultNow(null));
        }

        @Test
        void shouldReturnResult_WhenFutureIsDoneWithResult() {
            var value = 42L;
            var future = CompletableFuture.completedFuture(value);

            assertThat(KiwiFutures.resultNow(future)).isEqualTo(value);
        }

        @Test
        void shouldThrowIllegalState_WhenInterruptedExceptionOccurs() {
            KiwiFutures.interruptedExceptionHandler = e -> new IllegalStateException("I was rudely interrupted", e);

            Future<Long> future = new AlwaysInterruptingFuture();

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.resultNow(future))
                    .withMessage("I was rudely interrupted")
                    .withCauseExactlyInstanceOf(InterruptedException.class);
        }

        @Test
        void shouldThrowIllegalState_WhenFutureIsNotDone() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 42 * 42;
            });

            consumeThenCancel(future, theFuture -> assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.resultNow(theFuture))
                    .withMessage("Task has not completed"));
        }

        @Test
        void shouldThrowIllegalState_WhenFutureIsCancelled() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 42 * 84;
            });

            var wasCancelled = future.cancel(true);
            assertThat(wasCancelled).isTrue();

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.resultNow(future))
                    .withMessage("Task was cancelled")
                    .withCauseExactlyInstanceOf(CancellationException.class);
        }

        @Test
        void shouldThrowIllegalState_WhenFutureCompletesExceptionally() {
            var exception = new IOException("I/O error - device 123");
            var future = CompletableFuture.failedFuture(exception);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.resultNow(future))
                    .withMessage("Task completed with exception")
                    .withCause(exception);
        }
    }

    @Nested
    class ExceptionNow {

        @SuppressWarnings("ThrowableNotThrown")
        @Test
        void shouldThrowIllegalArgument_WhenGivenNullFuture() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiFutures.exceptionNow(null));
        }

        @Test
        void shouldReturnException_WhenIsDoneAndCompletesExceptionally() {
            var exception = new IOException("I/O error - device 456");
            var future = CompletableFuture.failedFuture(exception);

            assertThat(KiwiFutures.exceptionNow(future)).isSameAs(exception);
        }

        @SuppressWarnings("ThrowableNotThrown")
        @Test
        void shouldThrowIllegalState_WhenInterruptedExceptionOccurs() {
            KiwiFutures.interruptedExceptionHandler = e -> new IllegalStateException("Someone interrupted me!", e);

            Future<Long> future = new AlwaysInterruptingFuture();

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.exceptionNow(future))
                    .withMessage("Someone interrupted me!")
                    .withCauseExactlyInstanceOf(InterruptedException.class);
        }

        @SuppressWarnings("ThrowableNotThrown")
        @Test
        void shouldThrowIllegalState_WhenFutureIsNotDone() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 24 * 42;
            });

            consumeThenCancel(future, theFuture -> assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.exceptionNow(theFuture))
                    .withMessage("Task has not completed"));
        }

        @SuppressWarnings("ThrowableNotThrown")
        @Test
        void shouldThrowIllegalState_WhenFutureIsCancelled() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 24 * 84;
            });

            var wasCancelled = future.cancel(true);
            assertThat(wasCancelled).isTrue();

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.exceptionNow(future))
                    .withMessage("Task was cancelled")
                    .withNoCause();
        }

        @SuppressWarnings("ThrowableNotThrown")
        @Test
        void shouldThrowIllegalState_WhenFutureContainsResult() {
            var value = 84L;
            var future = CompletableFuture.completedFuture(value);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.exceptionNow(future))
                    .withMessage("Task completed with a result")
                    .withNoCause();
        }
    }

    @Nested
    class State {

        @Test
        void shouldThrowIllegalArgument_WhenGivenNullFuture() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiFutures.state(null));
        }

        @Test
        void shouldReturn_RUNNING_WhenIsNotDone() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 24 * 84;
            });

            consumeThenCancel(future, theFuture -> assertThat(KiwiFutures.state(future)).isEqualTo(FutureState.RUNNING));
        }

        @Test
        void shouldReturn_CANCELLED_WhenFutureWasCancelled() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 42 * 42;
            });

            var wasCancelled = future.cancel(true);
            assertThat(wasCancelled).isTrue();

            assertThat(KiwiFutures.state(future)).isEqualTo(FutureState.CANCELLED);
        }

        @Test
        void shouldReturn_SUCCESS_WhenCompletesWithResult() {
            var value = 84;
            var future = CompletableFuture.completedFuture(value);

            assertThat(KiwiFutures.state(future)).isEqualTo(FutureState.SUCCESS);
        }

        @Test
        void shouldThrowIllegalState_WhenInterruptedExceptionOccurs() {
            KiwiFutures.interruptedExceptionHandler = e -> new IllegalStateException("It was quite rude to interrupt me like that", e);

            Future<Long> future = new AlwaysInterruptingFuture();

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.state(future))
                    .withMessage("It was quite rude to interrupt me like that")
                    .withCauseExactlyInstanceOf(InterruptedException.class);
        }

        @Test
        void shouldReturn_FAILED_WhenCompletesExceptionally() {
            var exception = new IOException("I/O error - device 456");
            var future = CompletableFuture.failedFuture(exception);

            assertThat(KiwiFutures.state(future)).isEqualTo(FutureState.FAILED);
        }
    }

    @Nested
    class IsNotDone {

        @Test
        void shouldBeTrue_WhenFutureIsNotDone() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 24 * 42;
            });

            assertThat(KiwiFutures.isNotDone(future)).isTrue();

            var wasCancelled = future.cancel(true);
            assertThat(wasCancelled).isTrue();
        }

        @Test
        void shouldBeFalse_WhenFutureIsDone() {
            var future = CompletableFuture.completedFuture("foo");

            assertThat(KiwiFutures.isNotDone(future)).isFalse();
        }
    }

    private static <V> void consumeThenCancel(Future<V> future, Consumer<Future<V>> consumer) {
        try {
            consumer.accept(future);
        } finally {
            var wasCancelled = future.cancel(true);
            assertThat(wasCancelled)
                    .describedAs("Expected the Future to be cancelled but wasn't (was it already cancelled or completed?)")
                    .isTrue();
        }
    }

    /**
     * @implNote Using a "real mock" instead of Mockito here because it and avoids incurring
     * the "startup time" of Mockito.
     */
    static class AlwaysInterruptingFuture implements Future<Long> {

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Long get() throws InterruptedException {
            throw new InterruptedException("simulated interruption");
        }

        @Override
        public Long get(long timeout, @NonNull TimeUnit unit)
                throws InterruptedException {
            throw new InterruptedException("simulated interruption (with timeout)");
        }
    }
}
