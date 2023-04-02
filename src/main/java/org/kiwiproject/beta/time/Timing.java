package org.kiwiproject.beta.time;

import com.google.common.annotations.Beta;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.time.StopWatch;

import java.util.function.Supplier;

/**
 * Timing utilities that provide a convenient way to measure elapsed time of operations.
 */
@Beta
@UtilityClass
public class Timing {

    /**
     * Represents an operation that is timed.
     */
    public interface Timed {
        /**
         * @return the number of milliseconds that have elapsed since the operation started
         */
        long getElapsedMillis();
    }

    /**
     * Represents an operation that is timed and returns a result.
     *
     * @param <R> the result type
     */
    @Value
    public static class TimedWithResult<R> implements Timed {
        long elapsedMillis;
        R result;
    }

    /**
     * Represents an operation that is timed and returns no result.
     */
    @Value
    public static class TimedNoResult implements Timed {
        long elapsedMillis;
    }

    /**
     * Time an operation that returns a result using {@link StopWatch}. Any exception thrown by the
     * Supplier is propagated (not caught).
     * <p>
     * The {@link StopWatch} is reset before starting. It is always stopped, even if the operation
     * throws an exception.
     * <p>
     * If you need the elapsed time when an exception is thrown, you can simply call
     * {@link StopWatch#getTime()}.
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
    public static <R> TimedWithResult<R> timeWithResult(StopWatch stopWatch, Supplier<R> operation) {
        stopWatch.reset();
        stopWatch.start();
        R result;
        try {
            result = operation.get();
        } finally {
            stopWatch.stop();
        }
        return new TimedWithResult<>(stopWatch.getTime(), result);
    }

    /**
     * Time an operation that does not return a result using {@link StopWatch}. Any exception thrown by the
     * Supplier is propagated (not caught).
     * <p>
     * The {@link StopWatch} is reset before starting. It is always stopped, even if the operation
     * throws an exception.
     * <p>
     * If you need the elapsed time when an exception is thrown, you can simply call
     * {@link StopWatch#getTime()}.
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
    public static TimedNoResult timeNoResult(StopWatch stopWatch, Runnable operation) {
        stopWatch.reset();
        stopWatch.start();
        try {
            operation.run();
        } finally {
            stopWatch.stop();
        }
        return new TimedNoResult(stopWatch.getTime());
    }
}
