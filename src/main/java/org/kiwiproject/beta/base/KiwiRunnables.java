package org.kiwiproject.beta.base;

import com.google.common.annotations.Beta;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        @SuppressWarnings("java:S112")
        void run() throws Exception;
    }

    // TODO Add a default method in ThrowingRunnable to convert to Runnable and/or to CatchingRunnable?

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
     * @param runnnable the {@link ThrowingRunnable} to run
     */
    public static void runQuietly(ThrowingRunnable runnnable) {
        try {
            runnnable.run();
        } catch (Exception e) {
            LOG.warn("Suppressed exception:", e);
        }
    }

    // TODO Variants that accept actual Runnable??? Obviously they could only ignore RuntimeExceptions

    // TODO variants for Runnable and/or ThrowingRunnable that capture exceptions and return a List<RunResult>???
}
