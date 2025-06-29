package org.kiwiproject.beta.concurrent;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Utilities related to {@link Future}.
 */
@UtilityClass
@Beta
public class KiwiFutures {

    /**
     * This is the default handler for InterruptedException. It ensures that the current thread is re-interrupted by
     * calling {@link Thread#interrupt()} on the current thread, and then throws IllegalStateException with the
     * InterruptedException as the cause. This satisfies Sonar rule java:S2142 (InterruptedException should not be ignored).
     * In this class, we've suppressed Sonar's java:S2142 rule because we are interrupting the thread by default (just
     * not in test code).
     */
    private static final Function<InterruptedException, IllegalStateException> DEFAULT_INTERRUPTED_EXCEPTION_HANDLER = e -> {
        Thread.currentThread().interrupt();
        return new IllegalStateException("Task was interrupted", e);
    };

    /**
     * This is the handler for InterruptedException. This exists to facilitate testing the case when a Future throws
     * an InterruptedException, by allowing tests to replace it so that test threads are not interrupted.
     */
    @VisibleForTesting
    static Function<InterruptedException, IllegalStateException> interruptedExceptionHandler = DEFAULT_INTERRUPTED_EXCEPTION_HANDLER;

    /**
     * This can be used in test code to reset the InterruptedException handler. The easiest thing to do is to call this
     * method after each test.
     */
    @VisibleForTesting
    static void resetInterruptedExceptionHandler() {
        KiwiFutures.interruptedExceptionHandler = DEFAULT_INTERRUPTED_EXCEPTION_HANDLER;
    }

    /**
     * Represents the state of a {@link Future}.
     *
     * @implNote This is a copy of the Future.State enum that was added in JDK 19.
     */
    public enum FutureState {

        /**
         * The task has not completed.
         */
        RUNNING,

        /**
         * The task completed with a result.
         *
         * @see KiwiFutures#resultNow(Future)
         */
        SUCCESS,

        /**
         * The task completed with an exception.
         *
         * @see KiwiFutures#exceptionNow(Future)
         */
        FAILED,

        /**
         * The task was canceled.
         *
         * @see Future#cancel(boolean)
         */
        CANCELLED
    }

    /**
     * Returns the computed result without waiting.
     *
     * @param <V> the result type of the future
     * @param future the Future that should have completed with a result
     * @return the result of the task
     * @throws IllegalArgumentException if the future is null
     * @throws IllegalStateException if the task has not completed, was interrupted, or did not complete with a result
     * @apiNote JDK 19 adds {@code resultNow} as an instance method in {@link Future}, at which point this method
     * will no longer be needed. Code written for JDKs before 19 can use this method as a stand-in.
     * @implNote This implementation is mostly based on the JDK 19 code from {@code Future#resultNow}. The main
     * difference is that this implementation removes the {@code while} loop and handles the InterruptedException by
     * catching it, re-interrupting the current thread, and throwing an IllegalStateException. These changes were
     * made because it seems that the JDK 19 implementation has an infinite loop, since it uses a "while (true)" loop
     * which, when it catches InterruptedException, sets a boolean flag variable, but then allows execution to proceed.
     * The code will then return to the top of the loop, and unless I am missing something, will encounter another
     * InterruptedException when it calls {@code get} on the Future, and repeat forever (or until the program is
     * actually shut down). I suspect it is likely I am missing something as to why that loop exists in the JDK 19
     * implementation, but since I cannot explain it, that logic was removed from this implementation. See
     * <a href="https://github.com/openjdk/jdk19/blob/master/src/java.base/share/classes/java/util/concurrent/Future.java#L188">Future#resultNow</a>
     * for the JDK 19 implementation. If anyone happens upon this code and knows the reason why JDK 19 contains the
     * loop, please create an issue or discussion in <a href="https://github.com/sleberknight/kiwi-beta">kiwi-beta</a>.
     */
    @SuppressWarnings("java:S2142")
    public static <V> V resultNow(Future<V> future) {
        checkArgumentNotNull(future);

        if (isNotDone(future)) {
            throw new IllegalStateException("Task has not completed");
        }

        try {
            return future.get();
        } catch (InterruptedException e) {
            throw interruptedExceptionHandler.apply(e);
        } catch (ExecutionException e) {
            throw new IllegalStateException("Task completed with exception", e.getCause());
        } catch (CancellationException e) {
            throw new IllegalStateException("Task was cancelled", e);
        }
    }

    /**
     * Returns the exception thrown by the task without waiting.
     *
     * @param <V> the result type of the future
     * @param future the Future that should have completed with an Exception
     * @return the exception thrown by the task
     * @throws IllegalArgumentException if the future is null
     * @throws IllegalStateException if the task has not completed, was interrupted, completed normally, or was canceled
     * @apiNote JDK 19 adds {@code exceptionNow} as an instance method in {@link Future}, at which point this method
     * will no longer be needed. Code written for JDKs before 19 can use this method as a stand-in.
     * @implNote See the implementation note in {@link #resultNow(Future)}. The same loop-based implementation exists
     * in the JDK 19 {@code Future#exceptionNow}, so the same reasoning applies to this method (in terms of why this
     * method does not contain the same loop logic). See
     * <a href="https://github.com/openjdk/jdk19/blob/master/src/java.base/share/classes/java/util/concurrent/Future.java#L225">Future#exceptionNow</a>
     * for the JDK 19 implementation.
     */
    @SuppressWarnings("java:S2142")
    public static <V> Throwable exceptionNow(Future<V> future) {
        checkArgumentNotNull(future);

        if (isNotDone(future)) {
            throw new IllegalStateException("Task has not completed");
        }

        if (future.isCancelled()) {
            throw new IllegalStateException("Task was cancelled");
        }

        try {
            future.get();
            throw new IllegalStateException("Task completed with a result");
        } catch (InterruptedException e) {
            throw interruptedExceptionHandler.apply(e);
        } catch (ExecutionException e) {
            return e.getCause();
        }
    }

    /**
     * Returns the state of a {@link Future}.
     *
     * @param <V> the result type of the future
     * @param future the Future to check
     * @return the {@link FutureState} representing the Future's state
     * @throws IllegalArgumentException if the future is null
     * @throws IllegalStateException if the task was interrupted
     * @apiNote JDK 19 adds {@code state} as an instance method in {@link Future}, at which point this method
     * will no longer be needed. Code written for JDKs before 19 can use this method as a stand-in.
     * @implNote See the implementation note in {@link #resultNow(Future)}. The same loop-based implementation exists
     * in the JDK 19 {@code Future#state}, so the same reasoning applies to this method (in terms of why this
     * method does not contain the same loop logic). See
     * <a href="https://github.com/openjdk/jdk19/blob/master/src/java.base/share/classes/java/util/concurrent/Future.java#L282">Future#state</a>
     * for the JDK 19 implementation.
     */
    @SuppressWarnings("java:S2142")
    public static <V> FutureState state(Future<V> future) {
        checkArgumentNotNull(future);

        if (isNotDone(future)) {
            return FutureState.RUNNING;
        }

        if (future.isCancelled()) {
            return FutureState.CANCELLED;
        }

        try {
            future.get();
            return FutureState.SUCCESS;
        } catch (InterruptedException e) {
            throw interruptedExceptionHandler.apply(e);
        } catch (ExecutionException e) {
            return FutureState.FAILED;
        }
    }

    /**
     * Check whether a {@link Future} is done.
     * <p>
     * This is a convenience method for those who prefer (like me) to read code like:
     * <pre>
     * var future = ...;
     * if (isNotDone(future)) {
     *     // ...
     * }
     * </pre>
     * instead of code like:
     * <pre>
     * if (!future.isDone()) {
     *     // ...
     * }
     * </pre>
     * And since we can't add a {@code isNotDone()} method to Future, this is this best we can do in Java. And while
     * Lombok has {@code ExtensionMethod}, it is experimental so best to avoid.
     *
     * @param <V> the result type of the future
     * @param future the Future to check
     * @return true if the future is not done, and false if it is done (inverse of {@link Future#isDone})
     */
    public static <V> boolean isNotDone(Future<V> future) {
        return !future.isDone();
    }
}
