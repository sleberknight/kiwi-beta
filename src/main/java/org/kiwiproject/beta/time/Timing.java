package org.kiwiproject.beta.time;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.CheckReturnValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.kiwiproject.base.KiwiDeprecated;

import java.beans.ConstructorProperties;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Timing utilities that provide a convenient way to measure elapsed time of operations.
 */
@Beta
@UtilityClass
@Slf4j
public class Timing {

    // The maximum number of times to log a warning about the constructor
    // of TimedWithResult and TimedNoResult changing from millis to nanos.
    private static final int MAX_WARNINGS_TO_LOG = 10;

    /**
     * Represents an operation that is timed.
     */
    public sealed interface Timed permits TimedWithResult, TimedNoResult {
        /**
         * @return the number of milliseconds that elapsed during the operation
         */
        long getElapsedMillis();

        /**
         * @return the number of nanoseconds that elapsed during the operation
         */
        long getElapsedNanos();
    }

    /**
     * Represents an operation that is timed and returns a result.
     *
     * @param <R> the result type
     */
    @Getter
    @ToString
    @EqualsAndHashCode
    public static final class TimedWithResult<R> implements Timed {

        private static final AtomicInteger LOG_COUNT = new AtomicInteger();

        private final long elapsedMillis;
        private final long elapsedNanos;
        private final R result;

        /**
         * @implNote Technically, this won't be removed but will become private.
         * @deprecated since 3.0.0, for removal at 4.0.0, replaced by {@link #ofElapsedNanos(long, Object)}
         */
        @KiwiDeprecated(
                removeAt = "4.0.0",
                replacedBy = "TimedNotResult#ofElapsedMillis"
        )
        @Deprecated(since = "3.0.0", forRemoval = true)
        @SuppressWarnings({"java:S1133", "DeprecatedIsStillUsed"})
        @ConstructorProperties({"elapsedMillis", "result"})
        public TimedWithResult(long elapsedMillis, R result) {
            logElapsedMillisChangeToNanosWarning(TimedWithResult.class, LOG_COUNT);
            this.elapsedMillis = elapsedMillis;
            this.elapsedNanos = TimeUnit.MILLISECONDS.toNanos(elapsedMillis);
            this.result = result;
        }

        /**
         * Create an instance having elapsed milliseconds and a result.
         *
         * @param elapsedMillis the elapsed milliseconds
         * @param result        the result of the operation
         * @param <R>           the type of result returned by the operation
         * @return a new instance
         */
        @SuppressWarnings("java:S5738")
        public static <R> TimedWithResult<R> ofElapsedMillis(long elapsedMillis, R result) {
            return new TimedWithResult<>(elapsedMillis, result);
        }

        /**
         * Create an instance having elapsed nanoseconds and a result.
         *
         * @param elapsedNanos the elapsed nanoseconds
         * @param result       the result of the operation
         * @param <R>          the type of result returned by the operation
         * @return a new instance
         */
        @SuppressWarnings("java:S5738")
        public static <R> TimedWithResult<R> ofElapsedNanos(long elapsedNanos, R result) {
            return new TimedWithResult<>(TimeUnit.NANOSECONDS.toMillis(elapsedNanos), result);
        }

        @Override
        public long getElapsedNanos() {
            return elapsedNanos;
        }
    }

    /**
     * Represents an operation that is timed and returns no result.
     */
    @Getter
    @ToString
    @EqualsAndHashCode
    public static final class TimedNoResult implements Timed {

        private static final AtomicInteger LOG_COUNT = new AtomicInteger();

        private final long elapsedMillis;
        private final long elapsedNanos;

        /**
         * @deprecated since 3.0.0, for removal at 4.0.0, replaced by {@link #ofElapsedMillis(long)}
         * @implNote Technically, this won't be removed but will become private.
         */
        @KiwiDeprecated(
                removeAt = "4.0.0",
                replacedBy = "TimedNotResult#ofElapsedMillis"
        )
        @Deprecated(since = "3.0.0", forRemoval = true)
        @SuppressWarnings({"java:S1133", "DeprecatedIsStillUsed"})
        @ConstructorProperties({"elapsedMillis"})
        public TimedNoResult(long elapsedMillis) {
            logElapsedMillisChangeToNanosWarning(TimedNoResult.class, LOG_COUNT);
            this.elapsedMillis = elapsedMillis;
            this.elapsedNanos = TimeUnit.MILLISECONDS.toNanos(elapsedMillis);
        }

        /**
         * Create an instance having elapsed milliseconds.
         *
         * @param elapsedMillis the elapsed milliseconds
         * @return a new instance
         */
        @SuppressWarnings({"java:S5738"})
        public static TimedNoResult ofElapsedMillis(long elapsedMillis) {
            return new TimedNoResult(elapsedMillis);
        }

        /**
         * Create an instance having elapsed nanoseconds.
         *
         * @param elapsedNanos the elapsed nanoseconds
         * @return a new instance
         */
        @SuppressWarnings({"java:S5738"})
        public static TimedNoResult ofElapsedNanos(long elapsedNanos) {
            return new TimedNoResult(TimeUnit.NANOSECONDS.toMillis(elapsedNanos));
        }

        @Override
        public long getElapsedNanos() {
            return elapsedNanos;
        }
    }

    private static void logElapsedMillisChangeToNanosWarning(Class<?> clazz, AtomicInteger logCount) {
        if (logCount.get() >= MAX_WARNINGS_TO_LOG) {
            return;
        }

        logCount.incrementAndGet();

        LOG.warn("In a future release, elapsedMillis in the {} constructor will change to elapsedNanos," +
                " and the constructor may become private!" +
                " Any custom code using this directly should be changed to use 'ofElapsedMillis' to ensure" +
                " that it continues to report correct results after the constructor changes." +
                " Or, use 'ofElapsedNanos' and pass nanoseconds as the argument.", clazz.getName());
    }

    /**
     * Time an operation that returns a result using {@link StopWatch}. Any exception thrown by the
     * Supplier is propagated (not caught).
     * <p>
     * The {@link StopWatch} is reset before starting. It is always stopped, even if the operation
     * throws an exception.
     * <p>
     * If you need the elapsed time when an exception is thrown, you can simply call
     * {@link StopWatch#getDuration()} or {@link StopWatch#getNanoTime()}.
     * <p>
     * Since {@link StopWatch} is not thread-safe, callers are responsible for ensuring
     * thread-safety. Passing it as an argument permits timing more than one sequential operation
     * in the same method, but could lead to problems if callers are not careful. Typical use is
     * to instantiate the {@link StopWatch} within a method and use it only within that method.
     *
     * @param <R> the type of result returned by the operation
     * @param stopWatch the StopWatch to use
     * @param operation the operation to time
     * @return a {@link TimedWithResult} containing the elapsed time and the result of the operation
     */
    @CheckReturnValue
    public static <R> TimedWithResult<R> timeWithResult(StopWatch stopWatch, Supplier<R> operation) {
        stopWatch.reset();
        stopWatch.start();
        R result;
        try {
            result = operation.get();
        } finally {
            stopWatch.stop();
        }
        return TimedWithResult.ofElapsedNanos(stopWatch.getNanoTime(), result);
    }

    /**
     * Time an operation that does not return a result using {@link StopWatch}. Any exception thrown by the
     * Supplier is propagated (not caught).
     * <p>
     * The {@link StopWatch} is reset before starting. It is always stopped, even if the operation
     * throws an exception.
     * <p>
     * If you need the elapsed time when an exception is thrown, you can simply call
     * {@link StopWatch#getDuration()} or {@link StopWatch#getNanoTime()}.
     * <p>
     * Since {@link StopWatch} is not thread-safe, callers are responsible for ensuring
     * thread-safety. Passing it as an argument permits timing more than one sequential operation
     * in the same method, but could lead to problems if callers are not careful. Typical use is
     * to instantiate the {@link StopWatch} within a method and use it only within that method.
     *
     * @param stopWatch the StopWatch to use
     * @param operation the operation to time
     * @return a {@link TimedNoResult} containing the elapsed time of the operation
     */
    @CheckReturnValue
    public static TimedNoResult timeNoResult(StopWatch stopWatch, Runnable operation) {
        stopWatch.reset();
        stopWatch.start();
        try {
            operation.run();
        } finally {
            stopWatch.stop();
        }
        return TimedNoResult.ofElapsedNanos(stopWatch.getNanoTime());
    }

    /**
     * Represents an operation that is timed and may throw a runtime exception.
     *
     * @apiNote This uses RuntimeException rather than Exception because the timing
     * methods accept a Runnable or a Supplier, which can only throw RuntimeException.
     */
    public sealed interface TimedWithError
            permits TimedWithErrorOrResult, TimedWithErrorNoResult {

        /**
         * @return the number of nanoseconds that elapsed during the operation
         */
        long getElapsedNanos();

        /**
         * @return the number of milliseconds that elapsed during the operation
         */
        default long getElapsedMillis() {
            return TimeUnit.NANOSECONDS.toMillis(getElapsedNanos());
        }

        /**
         * @return true if the operation completed without exception, otherwise false
         */
        default boolean operationSucceeded() {
            return !hasException();
        }

        /**
         * @return true if the operation threw an exception, otherwise false
         */
        default boolean hasException() {
            return getException().isPresent();
        }

        /**
         * @return an Optional that will contain a RuntimeException if the operation failed.
         * If called when the operation succeeded, an empty Optional is always returned.
         */
        Optional<RuntimeException> getException();
    }

    /**
     * Represents an operation that is timed and returns a (possibly null) result but may throw an exception.
     *
     * @param <R> the result type
     */
    @Value
    public static class TimedWithErrorOrResult<R> implements TimedWithError {
        long elapsedNanos;
        R result;
        RuntimeException exception;

        /**
         * Create a new instance containing either a result or an exception.
         *
         * @param elapsedNanos the number of nanoseconds that elapsed during the operation
         * @param result the result of the operation; may be null
         * @param exception the exception thrown by the operation; may be null
         * @throws IllegalArgumentException if both result and exception are non-null
         */
        @VisibleForTesting
        TimedWithErrorOrResult(long elapsedNanos, R result, RuntimeException exception) {
            if (nonNull(result) && nonNull(exception)) {
                throw new IllegalArgumentException("Cannot contain a result and an exception");
            }

            this.elapsedNanos = elapsedNanos;
            this.result = result;
            this.exception = exception;
        }

        /**
         * Create a new instance containing a result.
         *
         * @param <R> the result type
         * @param elapsedNanos the number of nanoseconds that elapsed during the operation
         * @param result the result of the operation; may be null
         * @return a new instance representing a successful operation
         */
        public static <R> TimedWithErrorOrResult<R> ofResult(long elapsedNanos, R result) {
            return new TimedWithErrorOrResult<>(elapsedNanos, result, null);
        }

        /**
         * Create a new instance containing an exception.
         *
         * @param <R> the result type
         * @param elapsedNanos the number of nanoseconds that elapsed during the operation
         * @param exception the exception thrown by the operation
         * @return a new instance representing a failed operation
         */
        public static <R> TimedWithErrorOrResult<R> ofException(long elapsedNanos, RuntimeException exception) {
            checkArgumentNotNull(exception, "exception must not be null");
            return new TimedWithErrorOrResult<>(elapsedNanos, null, exception);
        }

        /**
         * @return true if the operation succeeded and contains a (possibly null) result
         */
        public boolean hasResult() {
            return operationSucceeded();
        }

        /**
         * @return an Optional that will contain a (possibly null) result when the operation succeeds.
         * If called when the operation failed, an empty Optional is always returned.
         */
        public Optional<R> getResult() {
            return Optional.ofNullable(result);
        }

        /**
         * @return true if the operation completed without exception and the actual result is null.
         */
        public boolean isNullResult() {
            return operationSucceeded() && getResult().isEmpty();
        }

        @Override
        public Optional<RuntimeException> getException() {
            return Optional.ofNullable(exception);
        }
    }

    /**
     * Represents an operation that is timed and returns no result but may throw an exception.
     */
    @Value
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class TimedWithErrorNoResult implements TimedWithError {
        long elapsedNanos;
        RuntimeException exception;

        /**
         * Create a new instance containing the elapsed time.
         *
         * @param elapsedNanos the number of nanoseconds that elapsed during the operation
         * @return a new instance representing a successful operation
         */
        public static TimedWithErrorNoResult ofSuccess(long elapsedNanos) {
            return new TimedWithErrorNoResult(elapsedNanos, null);
        }

        /**
         * Create a new instance containing an exception.
         *
         * @param elapsedNanos the number of nanoseconds that elapsed during the operation
         * @param exception the exception thrown by the operation
         * @return a new instance representing a failed operation
         */
        public static TimedWithErrorNoResult ofException(long elapsedNanos, RuntimeException exception) {
            checkArgumentNotNull(exception, "exception must not be null");
            return new TimedWithErrorNoResult(elapsedNanos, exception);
        }

        @Override
        public Optional<RuntimeException> getException() {
            return Optional.ofNullable(exception);
        }
    }

    /**
     * Time an operation that returns a result or throws an exception.
     * The return value should be inspected to determine whether the
     * operation succeeded or failed.
     *
     * @param <R>       the type of result returned by the operation
     * @param operation the operation to time
     * @return a {@link TimedWithErrorOrResult} containing the elapsed time
     * and the result of the operation or an exception if the operation failed
     */
    @CheckReturnValue
    public static <R> TimedWithErrorOrResult<R> timeWithResult(Supplier<R> operation) {
        var stopWatch = StopWatch.createStarted();
        try {
            var result = operation.get();
            return TimedWithErrorOrResult.ofResult(stopWatch.getNanoTime(), result);
        } catch (RuntimeException e) {
            return TimedWithErrorOrResult.ofException(stopWatch.getNanoTime(), e);
        } finally {
            stopWatch.stop();
        }
    }

    /**
     * Time an operation that does not return a result but may throw an exception.
     * The return value should be inspected to determine whether the
     * operation succeeded or failed.
     *
     * @param operation the operation to time
     * @return a {@link TimedWithErrorNoResult} containing the elapsed time of the operation
     * and an exception if the operation failed
     */
    @CheckReturnValue
    public static TimedWithErrorNoResult timeNoResult(Runnable operation) {
        var stopWatch = StopWatch.createStarted();
        try {
            operation.run();
            return TimedWithErrorNoResult.ofSuccess(stopWatch.getNanoTime());
        } catch (RuntimeException e) {
            return TimedWithErrorNoResult.ofException(stopWatch.getNanoTime(), e);
        } finally {
            stopWatch.stop();
        }
    }
}
