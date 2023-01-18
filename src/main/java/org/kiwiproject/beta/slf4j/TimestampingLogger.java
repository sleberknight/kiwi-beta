package org.kiwiproject.beta.slf4j;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;

import com.google.common.annotations.Beta;
import org.kiwiproject.base.KiwiStrings;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import javax.annotation.concurrent.NotThreadSafe;
import java.time.Duration;

/**
 * A simple way to log timing information about a repeated operation.
 * <p>
 * This is intended to be used for a single logical operation which contains multiple steps, or for the same operation
 * repeated over a collection. For example, an order operation with separate steps to find a user, create a new order
 * linked to the user, and insert the order to a database. Or, some kind of data migration that loops over records in
 * a source database table and writes to a new target database table.
 * <p>
 * <em>Currently, this is intended only to be used within a single thread.</em>
 */
@NotThreadSafe
@Beta
public class TimestampingLogger {

    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;
    private long previousTimestamp;

    /**
     * Create a new instance using the given {@link Logger}.
     */
    public TimestampingLogger(Logger logger) {
        this.logger = requireNotNull(logger);
    }

    /**
     * Logs a message and elapsed time message at TRACE level.
     *
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     * @see #logElapsed(Level, String, Object...)
     */
    public void traceLogElapsed(String message, Object... args) {
        logElapsed(Level.TRACE, message, args);
    }

    /**
     * Logs a message and elapsed time message at DEBUG level.
     *
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     * @see #logElapsed(Level, String, Object...)
     */
    public void debugLogElapsed(String message, Object... args) {
        logElapsed(Level.DEBUG, message, args);
    }

    /**
     * Logs the given message, and then logs the elapsed time since the previous log.
     * This results in two separate log messages.
     *
     * @param level   the level at which to log the message and elapsed time message
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     */
    public void logElapsed(Level level, String message, Object... args) {
        if (KiwiSlf4j.isEnabled(logger, level)) {
            var now = System.nanoTime();
            KiwiSlf4j.log(logger, level, message, args);
            logElapsedSincePreviousTimestamp(logger, level, now, previousTimestamp);
            previousTimestamp = now;
        }
    }

    private static void logElapsedSincePreviousTimestamp(Logger logger,
                                                         Level level,
                                                         long now,
                                                         long previousTimestamp) {
        if (previousTimestamp > 0) {
            var diffInNanos = now - previousTimestamp;
            KiwiSlf4j.log(logger, level,
                    "[elapsed time since previous: {} nanoseconds / {} millis]",
                    diffInNanos, Duration.ofNanos(diffInNanos).toMillis());
        } else {
            KiwiSlf4j.log(logger, level,
                    "[elapsed time since previous: N/A (no previous timestamp)]");
        }
    }

    /**
     * Logs a message at TRACE level and appends an elapsed time message.
     *
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     * @see #logAppendingElapsed(Level, String, Object...)
     */
    public void traceLogAppendingElapsed(String message, Object... args) {
        logAppendingElapsed(Level.TRACE, message, args);
    }

    /**
     * Logs a message at DEBUG level and appends an elapsed time message.
     *
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     * @see #logAppendingElapsed(Level, String, Object...)
     */
    public void debugLogAppendingElapsed(String message, Object... args) {
        logAppendingElapsed(Level.DEBUG, message, args);
    }

    /**
     * Logs a message at the given level and appends an elapsed time message.
     * This results in a single log message containing the original message followed by the elapsed time message.
     *
     * @param level   the level at which to log the message and elapsed time message
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     */
    public void logAppendingElapsed(Level level, String message, Object... args) {
        if (KiwiSlf4j.isEnabled(logger, level)) {
            var now = System.nanoTime();
            var formattedMessage = KiwiStrings.f(message, args);
            logAppendingElapsedSincePreviousTimestamp(logger, level, formattedMessage, now, previousTimestamp);
            previousTimestamp = now;
        }
    }

    private static void logAppendingElapsedSincePreviousTimestamp(Logger logger,
                                                                  Level level,
                                                                  String formattedMessage,
                                                                  long now,
                                                                  long previousTimestamp) {
        if (previousTimestamp > 0) {
            var diffInNanos = now - previousTimestamp;
            KiwiSlf4j.log(logger, level,
                    "{} [elapsed time since previous: {} nanoseconds / {} millis]",
                    formattedMessage, diffInNanos, Duration.ofNanos(diffInNanos).toMillis());
        } else {
            KiwiSlf4j.log(logger, level,
                    "{} [elapsed time since previous: N/A (no previous timestamp)]",
                    formattedMessage);
        }
    }
}
