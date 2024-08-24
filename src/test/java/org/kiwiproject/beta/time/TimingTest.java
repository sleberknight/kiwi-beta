package org.kiwiproject.beta.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.beta.time.Timing.TimedNoResult;
import org.kiwiproject.beta.time.Timing.TimedWithErrorNoResult;
import org.kiwiproject.beta.time.Timing.TimedWithErrorOrResult;
import org.kiwiproject.beta.time.Timing.TimedWithResult;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@DisplayName("Timing")
class TimingTest {

    private static final KiwiEnvironment ENV = new DefaultEnvironment();

    @Nested
    class WithExplicitStopWatch {

        private StopWatch stopWatch;

        @BeforeEach
        void setUp() {
            stopWatch = new StopWatch();
        }

        @RepeatedTest(10)
        void shouldTimeOperationsThatReturnResult() {
            Supplier<Integer> op = () -> {
                sleepSmallRandomTime();
                return 42 * 42;
            };
            var result = Timing.timeWithResult(stopWatch, op);

            assertAll(
                    () -> assertThat(result.getElapsedMillis()).isPositive(),
                    () -> assertThat(result.getResult()).isEqualTo(1764),
                    () -> assertThat(stopWatch.isStopped()).isTrue(),
                    () -> assertThat(stopWatch.getTime()).isEqualTo(result.getElapsedMillis())
            );
        }

        @RepeatedTest(10)
        void shouldStopStopWatch_WhenExceptionsAreThrown_FromTimedOperationWithResult() {
            Supplier<Integer> op = () -> {
                sleepSmallRandomTime();
                throw new RuntimeException("oops");
            };

            assertAll(
                    () -> assertThatThrownBy(() -> Timing.timeWithResult(stopWatch, op))
                            .isExactlyInstanceOf(RuntimeException.class)
                            .hasMessage("oops"),
                    () -> assertThat(stopWatch.isStopped()).isTrue(),
                    () -> assertThat(stopWatch.getTime()).isPositive()
            );
        }

        @RepeatedTest(10)
        void shouldTimeOperationsThatDoNotReturnResult() {
            Runnable op = TimingTest::sleepSmallRandomTime;
            var result = Timing.timeNoResult(stopWatch, op);

            assertAll(
                    () -> assertThat(result.getElapsedMillis()).isPositive(),
                    () -> assertThat(stopWatch.isStopped()).isTrue(),
                    () -> assertThat(stopWatch.getTime()).isEqualTo(result.getElapsedMillis())
            );
        }

        @RepeatedTest(10)
        void shouldStopStopWatch_WhenExceptionsAreThrown_FromTimedOperationWithNoResult() {
            Runnable op = () -> {
                sleepSmallRandomTime();
                throw new RuntimeException("ugh");
            };

            assertAll(
                    () -> assertThatThrownBy(() -> Timing.timeNoResult(stopWatch, op))
                            .isExactlyInstanceOf(RuntimeException.class)
                            .hasMessage("ugh"),
                    () -> assertThat(stopWatch.isStopped()).isTrue(),
                    () -> assertThat(stopWatch.getTime()).isPositive()
            );
        }
    }

    @Nested
    class TimedWithResultClass {

        @Test
        void shouldCreateUsing_OfElapsedMillis() {
            var millis = 840;
            var result = TimedWithResult.ofElapsedMillis(millis, "the result");

            assertAll(
                    () -> assertThat(result.getResult()).isEqualTo("the result"),
                    () -> assertThat(result.getElapsedMillis()).isEqualTo(millis),
                    () -> assertThat(result.getElapsedNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(millis))
            );
        }

        @Test
        void shouldCreateUsing_OfElapsedNanos() {
            var millis = 420;
            var nanos = TimeUnit.MILLISECONDS.toNanos(millis);
            var result = TimedWithResult.ofElapsedNanos(nanos, "the result");

            assertAll(
                    () -> assertThat(result.getResult()).isEqualTo("the result"),
                    () -> assertThat(result.getElapsedMillis()).isEqualTo(millis),
                    () -> assertThat(result.getElapsedNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(millis))
            );
        }

        @RepeatedTest(25)
        void shouldOnlyLogTenWarnings_RunManually_AndInspectOutput() {
            var elapsedNanos = 84_000_000;
            var result = TimedWithResult.ofElapsedNanos(elapsedNanos, "a result");
            assertThat(result.getElapsedMillis()).isEqualTo(TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
        }
    }

    @Nested
    class TimedNoResultClass {

        @Test
        void shouldCreateUsing_OfElapsedMillis() {
            var millis = 840;
            var result = TimedNoResult.ofElapsedMillis(millis);

            assertAll(
                    () -> assertThat(result.getElapsedMillis()).isEqualTo(millis),
                    () -> assertThat(result.getElapsedNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(millis))
            );
        }

        @Test
        void shouldCreateUsing_OfElapsedNanos() {
            var millis = 420;
            var nanos = TimeUnit.MILLISECONDS.toNanos(millis);
            var result = TimedNoResult.ofElapsedNanos(nanos);

            assertAll(
                    () -> assertThat(result.getElapsedMillis()).isEqualTo(millis),
                    () -> assertThat(result.getElapsedNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(millis))
            );
        }

        @RepeatedTest(25)
        void shouldOnlyLogTenWarnings_RunManually_AndInspectOutput() {
            var elapsedNanos = 42_000_000;
            var result = TimedNoResult.ofElapsedNanos(elapsedNanos);
            assertThat(result.getElapsedMillis()).isEqualTo(TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
        }
    }

    @Nested
    class TimedWithErrorOrResultClass {

        @Test
        void shouldThrowIllegalArgument_WhenBothConstructorArgsAreNotNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new TimedWithErrorOrResult<>(42_000_000L, "the result", new RuntimeException()))
                    .withMessage("Cannot contain a result and an exception");
        }

        @Test
        void shouldThrowIllegalArgument_WhenNullExceptionIsProvided() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TimedWithErrorOrResult.ofException(42_000_000L, null))
                    .withMessage("exception must not be null");
        }

        @Test
        void shouldCreateWithNonNullResult() {
            var result = new TimedWithErrorOrResult<>(42_000_000L, "the result", null);

            assertAll(
                () -> assertThat(result.getElapsedNanos()).isEqualTo(42_000_000L),
                () -> assertThat(result.getElapsedMillis()).isEqualTo(42L),
                () -> assertThat(result.operationSucceeded()).isTrue(),
                () -> assertThat(result.hasResult()).isTrue(),
                () -> assertThat(result.isNullResult()).isFalse(),
                () -> assertThat(result.getResult()).contains("the result"),
                () -> assertThat(result.hasException()).isFalse(),
                () -> assertThat(result.getException()).isEmpty()
            );
        }

        @Test
        void shouldCreateWithNullResult() {
            var result = new TimedWithErrorOrResult<>(420_000_000L, null, null);

            assertAll(
                () -> assertThat(result.getElapsedNanos()).isEqualTo(420_000_000L),
                () -> assertThat(result.getElapsedMillis()).isEqualTo(420L),
                () -> assertThat(result.operationSucceeded()).isTrue(),
                () -> assertThat(result.hasResult()).isTrue(),
                () -> assertThat(result.isNullResult()).isTrue(),
                () -> assertThat(result.getResult()).isEmpty(),
                () -> assertThat(result.hasException()).isFalse(),
                () -> assertThat(result.getException()).isEmpty()
            );
        }

        @Test
        void shouldCreateWithException() {
            var error = new UncheckedIOException(new IOException("disk full"));
            var result = new TimedWithErrorOrResult<>(126_000_000L, null, error);

            assertAll(
                () -> assertThat(result.getElapsedMillis()).isEqualTo(126L),
                () -> assertThat(result.operationSucceeded()).isFalse(),
                () -> assertThat(result.hasResult()).isFalse(),
                () -> assertThat(result.isNullResult()).isFalse(),
                () -> assertThat(result.getResult()).isEmpty(),
                () -> assertThat(result.hasException()).isTrue(),
                () -> assertThat(result.getException()).contains(error)
            );
        }
    }

    @Nested
    class TimedWithErrorNoResultClass {

        @Test
        void shouldCreateWithoutException() {
            var result = TimedWithErrorNoResult.ofSuccess(42_000_000L);

            assertAll(
                () -> assertThat(result.getElapsedNanos()).isEqualTo(42_000_000L),
                () -> assertThat(result.getElapsedMillis()).isEqualTo(42L),
                () -> assertThat(result.operationSucceeded()).isTrue(),
                () -> assertThat(result.hasException()).isFalse(),
                () -> assertThat(result.getException()).isEmpty()
            );
        }

        @Test
        void shouldCreateWithException() {
            var error = new UncheckedIOException(new IOException("broken pipe"));
            var result = TimedWithErrorNoResult.ofException(420_000_000L, error);

            assertAll(
                () -> assertThat(result.getElapsedNanos()).isEqualTo(420_000_000L),
                () -> assertThat(result.getElapsedMillis()).isEqualTo(420L),
                () -> assertThat(result.operationSucceeded()).isFalse(),
                () -> assertThat(result.hasException()).isTrue(),
                () -> assertThat(result.getException()).contains(error)
            );
        }

        @Test
        void shouldThrowIllegalArgument_WhenNullExceptionIsProvided() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TimedWithErrorNoResult.ofException(42_000_000L, null))
                    .withMessage("exception must not be null");
        }
    }

    @Nested
    class WithInternalStopWatch {

        @RepeatedTest(10)
        void shouldTimeOperationsThatReturnResult() {
            Supplier<Integer> op = () -> {
                sleepSmallRandomTime();
                return 42 * 42;
            };
            var result = Timing.timeWithResult(op);

            assertAll(
                    () -> assertThat(result.getElapsedNanos()).isPositive(),
                    () -> assertThat(result.getElapsedMillis())
                            .isEqualTo(TimeUnit.NANOSECONDS.toMillis(result.getElapsedNanos())),
                    () -> assertThat(result.getElapsedMillis()).isPositive(),
                    () -> assertThat(result.operationSucceeded()).isTrue(),
                    () -> assertThat(result.hasResult()).isTrue(),
                    () -> assertThat(result.getResult()).contains(1764),
                    () -> assertThat(result.hasException()).isFalse(),
                    () -> assertThat(result.getException()).isEmpty()
            );
        }

        @RepeatedTest(10)
        void shouldTimedOperationsThatThrowException() {
            var exception = new RuntimeException("oops");
            Supplier<Integer> op = () -> {
                sleepSmallRandomTime();
                throw exception;
            };

            var result = Timing.timeWithResult(op);

            assertAll(
                () -> assertThat(result.getElapsedNanos()).isPositive(),
                () -> assertThat(result.getElapsedMillis())
                        .isEqualTo(TimeUnit.NANOSECONDS.toMillis(result.getElapsedNanos())),
                () -> assertThat(result.getElapsedMillis()).isPositive(),
                () -> assertThat(result.operationSucceeded()).isFalse(),
                () -> assertThat(result.hasResult()).isFalse(),
                () -> assertThat(result.getResult()).isEmpty(),
                () -> assertThat(result.hasException()).isTrue(),
                () -> assertThat(result.getException()).contains(exception)
            );
        }

        @RepeatedTest(10)
        void shouldTimeOperationsThatDoNotReturnResult() {
            Runnable op = TimingTest::sleepSmallRandomTime;
            var result = Timing.timeNoResult(op);

            assertAll(
                    () -> assertThat(result.getElapsedNanos()).isPositive(),
                    () -> assertThat(result.getElapsedMillis())
                            .isEqualTo(TimeUnit.NANOSECONDS.toMillis(result.getElapsedNanos())),
                    () -> assertThat(result.getElapsedMillis()).isPositive(),
                    () -> assertThat(result.operationSucceeded()).isTrue(),
                    () -> assertThat(result.hasException()).isFalse(),
                    () -> assertThat(result.getException()).isEmpty()
            );
        }

        @RepeatedTest(10)
        void shouldStopStopWatch_WhenExceptionsAreThrown_FromTimedOperationWithNoResult() {
            var exception = new RuntimeException("ugh");
            Runnable op = () -> {
                sleepSmallRandomTime();
                throw exception;
            };

            var result = Timing.timeNoResult(op);

            assertAll(
                    () -> assertThat(result.getElapsedNanos()).isPositive(),
                    () -> assertThat(result.getElapsedMillis())
                            .isEqualTo(TimeUnit.NANOSECONDS.toMillis(result.getElapsedNanos())),
                    () -> assertThat(result.getElapsedMillis()).isPositive(),
                    () -> assertThat(result.operationSucceeded()).isFalse(),
                    () -> assertThat(result.hasException()).isTrue(),
                    () -> assertThat(result.getException()).contains(exception)
            );
        }
    }

    private static void sleepSmallRandomTime() {
        ENV.sleepQuietly(ThreadLocalRandom.current().nextLong(10, 20));
    }
}
