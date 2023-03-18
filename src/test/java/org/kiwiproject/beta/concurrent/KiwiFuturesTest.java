package org.kiwiproject.beta.concurrent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.DefaultEnvironment;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

class KiwiFuturesTest {

    @Nested
    class ResultNow {

        @Test
        void shouldReturnResult_WhenFutureIsDoneWithResult() {
            var value = 42L;
            var future = CompletableFuture.completedFuture(value);

            assertThat(KiwiFutures.resultNow(future)).isEqualTo(value);
        }

        @Test
        void shouldThrowIllegalState_WhenFutureIsNotDone() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 42 * 42;
            });

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.resultNow(future))
                    .withMessage("Task has not completed");
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

        @Test
        void shouldReturnException_WhenIsDoneAndCompletesExceptionally() {
            var exception = new IOException("I/O error - device 456");
            var future = CompletableFuture.failedFuture(exception);

            assertThat(KiwiFutures.exceptionNow(future)).isSameAs(exception);
        }

        @Test
        void shouldThrowIllegalState_WhenFutureIsNotDone() {
            var future = CompletableFuture.supplyAsync(() -> {
                new DefaultEnvironment().sleepQuietly(10, TimeUnit.SECONDS);
                return 24 * 42;
            });

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiFutures.exceptionNow(future))
                    .withMessage("Task has not completed");
        }

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
}
