package org.kiwiproject.beta.base;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import org.kiwiproject.util.function.KiwiConsumers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Copied and modified from kiwi's {@link org.kiwiproject.base.CatchingRunnable}. The changes include:
 * <ul>
 * <li>The ability to provide a name, which is used when reporting unexpected exceptions</li>
 * <li>Factory methods to easily create an instance with or without a name and exception handler</li>
 * </ul>
 * These features may or may not ever be added to kiwi.
 * <p>
 * Original documentation from kiwi:
 * <p>
 * Extension of {@link Runnable} that never lets exceptions escape. Useful for things like scheduled executions
 * using {@link java.util.concurrent.ScheduledExecutorService} where an intermittent error should not cause the
 * executor to suppress future executions (which is the default behavior).
 */
@Beta
@FunctionalInterface
public interface CatchingRunnable2 extends Runnable {

    /**
     * Create a new instance wrapping the given Runnable.
     */
    static CatchingRunnable2 of(Runnable runnable) {
        return of(null, runnable);
    }

    /**
     * Create a new instance wrapping the given Runnable. Uses the given Consumer to handle
     * unexpected exceptions thrown by the Runnable.
     */
    static CatchingRunnable2 of(Runnable runnable, Consumer<Exception> exceptionHandler) {
        return of(null, runnable, exceptionHandler);
    }

    /**
     * Create a new instance with the given name that wraps the given Runnable.
     */
    static CatchingRunnable2 of(String name, Runnable runnable) {
        return of(name, runnable, KiwiConsumers.noOp());
    }

    /**
     * Create a new named-instance wrapping the given Runnable. Uses the given Consumer to handle
     * unexpected exceptions thrown by the Runnable.
     */
    static CatchingRunnable2 of(String name, Runnable runnable, Consumer<Exception> exceptionHandler) {
        checkArgumentNotNull(runnable, "runnable must not be null");
        checkArgumentNotNull(exceptionHandler, "handler must not be null");

        return new CatchingRunnable2() {
            @Override
            public Optional<String> name() {
                return Optional.ofNullable(name);
            }

            @Override
            public void runSafely() {
                runnable.run();
            }

            @Override
            public void handleExceptionSafely(Exception exception) {
                exceptionHandler.accept(exception);
            }
        };
    }

    /**
     * Returns a name, if available, which can be used to differentiate instances.
     * The name is used when logging exceptions thrown by the Runnable or by the
     * exception handler defined in {@link #handleExceptionSafely}.
     * <p>
     * For example, if an application uses multiple scheduled tasks to perform
     * background actions and wraps those tasks with {@link CatchingRunnable2}, then
     * providing a name can provide context when logging exceptions.
     *
     * @return an Optional with the name of this instance
     */
    default Optional<String> name() {
        return Optional.empty();
    }

    /**
     * Wraps {@link #runSafely()} in a try/catch. Logs exceptions and will call {@link #handleExceptionSafely(Exception)}
     * to permit handling of any thrown exceptions.
     */
    @Override
    default void run() {
        try {
            runSafely();
        } catch (Exception e) {
            getLogger().error("Error occurred calling runSafely{}", description(), e);

            try {
                handleExceptionSafely(e);
            } catch (Exception ex) {
                getLogger().error("Error occurred calling handleExceptionSafely{}", description(), ex);
            }
        }
    }

    private static Logger getLogger() {
        return LoggerFactory.getLogger(CatchingRunnable2.class);
    }

    private String description() {
        return name().map(name -> " [" + name + "]").orElse("");
    }

    /**
     * Handle an exception thrown by {@link #runSafely()}.
     *
     * @param exception the {@link Exception} to handle
     */
    default void handleExceptionSafely(Exception exception) {
        // no-op by default; override if desired
    }

    /**
     * The logic that could throw a {@link RuntimeException}.
     */
    void runSafely();
}
