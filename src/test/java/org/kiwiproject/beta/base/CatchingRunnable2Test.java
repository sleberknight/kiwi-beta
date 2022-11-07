package org.kiwiproject.beta.base;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.FIVE_SECONDS;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test copied from kiwi's CatchingRunnableTest, and new tests added for
 * the new {@link CatchingRunnable2} additions including the factory methods
 * and providing a name.
 */
@DisplayName("CatchingRunnable2")
@Slf4j
class CatchingRunnable2Test {

    private AtomicInteger callCount;

    @BeforeEach
    void setUp() {
        callCount = new AtomicInteger();
    }

    // Begin - Tests that are not in kiwi's CatchingRunnableTest

    @Test
    void shouldCreateUsingFactory_WithRunnable() {
        var safeRunnable = CatchingRunnable2.of(callCount::incrementAndGet);
        assertThat(safeRunnable.name()).isEmpty();

        safeRunnable.run();

        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldCreateUsingFactory_WithNameAndRunnable() {
        var safeRunnable = CatchingRunnable2.of("incrementAndGet-1", callCount::incrementAndGet);
        assertThat(safeRunnable.name()).contains("incrementAndGet-1");

        safeRunnable.run();

        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldAllowNullNameUsingFactoryMethod() {
        var safeRunnable = CatchingRunnable2.of(null, callCount::incrementAndGet);
        assertThat(safeRunnable.name()).isEmpty();

        safeRunnable.run();

        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldRequireRunnable_InFactoryMethod() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CatchingRunnable2.of(null))
                .withMessage("runnable must not be null");
    }

    @Test
    void shouldRequireRunnable_InFactoryMethodWithName() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CatchingRunnable2.of("the name", null))
                .withMessage("runnable must not be null");
    }

    @Test
    void shouldSuppressIt_WhenAnExceptionIsThrown_ByFactoryCreatedInstance() {
        var safeRunnable = CatchingRunnable2.of("unsafe_runnable_1", () -> {
            callCount.incrementAndGet();
            throw new IllegalStateException("The bar is not ready to baz because corge is unavailable");
        });

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldSuppressIt_WhenAnExceptionIsThrown_ByFactoryCreatedInstance_AndErrorOccursHandlingTheException() {
        var handleExceptionCount = new AtomicInteger();
        var safeRunnable = CatchingRunnable2.of("unsafe_runnable_2",
                () -> {
                    callCount.incrementAndGet();
                    throw new IllegalStateException("This is the original error");
                }, exception -> {
                    handleExceptionCount.incrementAndGet();
                    throw new RuntimeException("This is the error handling the error!");
                });

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
        assertThat(handleExceptionCount).hasValue(1);
    }

    // End - Tests that are not in kiwi's CatchingRunnableTest

    @Test
    void shouldRunNormally_WhenNoExceptionIsThrown() {
        CatchingRunnable2 safeRunnable = callCount::incrementAndGet;
        safeRunnable.run();

        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldSuppressIt_WhenAnExceptionIsThrown() {
        CatchingRunnable2 safeRunnable = () -> {
            callCount.incrementAndGet();
            throw new IllegalStateException("The bar is not ready to baz because corge is unavailable");
        };

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldSuppressIt_WhenAnExceptionThrown_AndErrorOccursHandlingTheException() {
        var handleExceptionCount = new AtomicInteger();
        var safeRunnable = new CatchingRunnable2() {
            @Override
            public void runSafely() {
                callCount.incrementAndGet();
                throw new IllegalStateException("This is the original error");
            }

            @Override
            public void handleExceptionSafely(Exception exception) {
                handleExceptionCount.incrementAndGet();
                throw new RuntimeException("This is the error handling the error!");
            }
        };

        assertThatCode(safeRunnable::run).doesNotThrowAnyException();
        assertThat(callCount).hasValue(1);
        assertThat(handleExceptionCount).hasValue(1);
    }

    @Test
    void shouldNotSuppressIt_WhenErrorIsThrown() {
        CatchingRunnable2 safeRunnable = () -> {
            callCount.incrementAndGet();
            throw new Error("The bar is not ready to baz because corge is unavailable");
        };

        assertThatThrownBy(safeRunnable::run)
                .describedAs("Error should not be caught by CatchingRunnable")
                .isExactlyInstanceOf(Error.class)
                .hasMessageStartingWith("The bar is not ready");
        assertThat(callCount).hasValue(1);
    }

    @Test
    void shouldNotSuppressIt_WhenSneakyCatchingRunnable_ThrowsThrowable() {
        var runnable = new SneakyThrowableThrowingRunnable();

        assertThatThrownBy(runnable::run)
                .describedAs("Sneakily thrown Throwable should not be caught by CatchingRunnable")
                .isExactlyInstanceOf(Throwable.class)
                .hasMessage("I am really, really, very bad");
        assertThat(runnable.callCount).hasValue(1);
    }

    private static class SneakyThrowableThrowingRunnable implements CatchingRunnable2 {

        AtomicInteger callCount = new AtomicInteger();

        @SneakyThrows
        @Override
        public void runSafely() {
            callCount.incrementAndGet();
            throw new Throwable("I am really, really, very bad");
        }
    }

    @SuppressWarnings("CatchMayIgnoreException")
    @Test
    void shouldNotTerminateExecution_OfScheduledExecutor_WhenExceptionsAreThrown() throws InterruptedException {
        var scheduledExecutorService = Executors.newScheduledThreadPool(1);
        var errorCount = new AtomicInteger();

        Future<?> scheduledFuture = null;
        try {
            CatchingRunnable2 safeRunnable = () -> {
                callCount.incrementAndGet();
                if (anErrorOccurred()) {
                    errorCount.incrementAndGet();
                    throw new RuntimeException("Chance dictated this...");
                }
            };
            LOG.debug("Perform action at: {}", Instant.now());
            scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(
                    safeRunnable, 0L, 10L, TimeUnit.MILLISECONDS);

            var minimumCallCount = 15;
            await().atMost(FIVE_SECONDS).until(() -> callCount.get() >= minimumCallCount);

            LOG.debug("call count: {}, error count: {}", callCount.get(), errorCount.get());

            assertThat(scheduledFuture)
                    .describedAs("Should still be executing")
                    .isNotCancelled();
            assertThat(callCount).hasValueGreaterThanOrEqualTo(minimumCallCount);
            assertThat(errorCount)
                    .describedAs("We should have received at least one error for proper verification")
                    .hasPositiveValue();

        } catch (Exception e) {
            fail("No exceptions should have escaped", e);
        } finally {
            if (nonNull(scheduledFuture)) {
                scheduledFuture.cancel(true);
            }
            scheduledExecutorService.shutdown();
            var terminated = scheduledExecutorService.awaitTermination(100, TimeUnit.MILLISECONDS);
            LOG.info("Terminated successfully: {}", terminated);
        }
    }

    private boolean anErrorOccurred() {
        return ThreadLocalRandom.current().nextInt(10) < 5;
    }
}
