package org.kiwiproject.beta.base;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.base.CatchingRunnable;

import java.util.Arrays;
import java.util.List;

/**
 * Static utilities related to {@link Runnable}.
 */
@UtilityClass
@Slf4j
@Beta
public class KiwiRunnables {

    /**
     * A variation on {@link Runnable} which allows exceptions to be thrown.
     * <p>
     * Note that since it has no relation to {@link Runnable} it can't be used in things like Java's
     * concurrency classes that expect {@link Runnable}. However, you can use the provided conversion
     * methods to convert into {@link Runnable} objects.
     */
    @FunctionalInterface
    public interface ThrowingRunnable {

        /**
         * Does something that might throw a checked exception.
         *
         * @throws Exception if something went wrong
         */
        @SuppressWarnings("java:S112")
        void run() throws Exception;

        /**
         * Convert a plain {@link Runnable} into a {@link ThrowingRunnable}.
         *
         * @param runnable the Runnable to convert
         * @return a new ThrowingRunnable that wraps the original Runnable
         */
        static ThrowingRunnable of(Runnable runnable) {
            return runnable::run;
        }

        /**
         * Converts this instance to a {@link Runnable} which catches any {@link Exception} that is thrown and wraps
         * it in a {@link RuntimeException}.
         *
         * @return a new {@link Runnable} instance
         */
        default Runnable toRunnable() {
            var outer = this;
            return () -> {
                try {
                    outer.run();
                } catch (Exception e) {
                    throw new WrappedException(e);
                }
            };
        }

        /**
         * Converts this instance to a {@link CatchingRunnable}.
         *
         * @return a new CatchingRunnable instance
         */
        default CatchingRunnable toCatchingRunnable() {
            var outer = this;
            return () -> {
                try {
                    outer.run();
                } catch (Exception e) {
                    throw new WrappedException(e);
                }
            };
        }

        /**
         * Converts this instance to a {@link CatchingRunnable2}.
         *
         * @return a new CatchingRunnable2 instance
         */
        default CatchingRunnable2 toCatchingRunnable2() {
            return CatchingRunnable2.of(toRunnable());
        }
    }

    /**
     * A {@link RuntimeException} that contains a checked exception thrown by a {@link ThrowingRunnable}.
     * <p>
     * This exception type is intended to be used when converting {@link ThrowingRunnable} to a {@link Runnable}, which
     * cannot throw checked exceptions.
     */
    public static class WrappedException extends RuntimeException {
        public WrappedException(Throwable cause) {
            super("Contains Exception thrown by a wrapped ThrowingRunnable", cause);
        }
    }

    /**
     * Run all the {@link ThrowingRunnable} instances, ignoring exceptions.
     *
     * @param runnables the {@link ThrowingRunnable}s to run
     */
    public static void runAllQuietly(ThrowingRunnable... runnables) {
        Arrays.stream(runnables).forEach(KiwiRunnables::runQuietly);
    }

    /**
     * Run the {@link ThrowingRunnable} instance, ignoring exceptions.
     *
     * @param runnable the {@link ThrowingRunnable} to run
     */
    public static void runQuietly(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            LOG.warn("Suppressed exception:", e);
        }
    }

    /**
     * Represents the result of an attempt to run a {@link ThrowingRunnable}.
     */
    @Accessors(fluent = true)
    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class RunResult {

        /**
         * If an error occurred while running an object, this contains the {@link Exception} that was thrown.
         * If there was no error, this will be {@code null}.
         */
        @Nullable
        private Exception error;

        /**
         * Create a new "successfully run" instance.
         *
         * @return a new instance representing a successful run
         */
        public static RunResult ofSuccess() {
            return new RunResult(null);
        }

        /**
         * Create a new "failed run" instance.
         *
         * @param exception the error that occurred while running an object
         * @return a new instance representing the failed run
         */
        public static RunResult ofError(Exception exception) {
            checkArgumentNotNull(exception);
            return new RunResult(exception);
        }

        /**
         * @return true if the operation succeeded; otherwise false
         */
        public boolean success() {
            return isNull(error);
        }

        /**
         * @return true if the operation failed; otherwise false
         */
        public boolean hasError() {
            return nonNull(error);
        }
    }

    /**
     * Run all the given {@link ThrowingRunnable} objects, and return a single {@link RunResult} corresponding to
     * each input object in order.
     *
     * @param runnables the {@link ThrowingRunnable}s to run
     * @return a List containing a {@link RunResult} corresponding to each {@link ThrowingRunnable} argument, in order
     */
    public static List<RunResult> runAll(ThrowingRunnable... runnables) {
        return Arrays.stream(runnables).map(KiwiRunnables::run).toList();
    }

    /**
     * Run the given {@link ThrowingRunnable}.
     *
     * @param runnable the {@link ThrowingRunnable} to run
     * @return the {@link RunResult}
     */
    public static RunResult run(ThrowingRunnable runnable) {
        try {
            runnable.run();
            return RunResult.ofSuccess();
        } catch (Exception e) {
            return RunResult.ofError(e);
        }
    }
}
