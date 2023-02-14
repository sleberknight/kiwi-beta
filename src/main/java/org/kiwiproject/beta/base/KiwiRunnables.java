package org.kiwiproject.beta.base;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.base.CatchingRunnable;

import java.util.Arrays;

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

    // TODO variants that capture exceptions and return a List<RunResult>???
}
