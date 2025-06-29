package org.kiwiproject.beta.slf4j;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requirePositiveOrZero;

import com.google.common.annotations.Beta;
import lombok.Builder;
import org.kiwiproject.base.KiwiStrings;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.time.Duration;
import java.util.function.BiFunction;

/**
 * A simple way to log timing information about a repeated operation.
 * <p>
 * This is intended to be used for a single logical operation which contains multiple steps, or for the same operation
 * repeated over a collection. For example, an order operation with separate steps to find a user, create a new order
 * linked to the user, and insert the order to a database. Or, some kind of data migration that loops over records in
 * a source database table and writes to a new target database table.
 * <p>
 * Use the single argument constructor to create an instance with default values. Otherwise, use the {@link #builder()}
 * to customize the behavior.
 * <p>
 * The options provided via the builder are:
 * <table>
 *     <caption>Options provided via the builder</caption>
 *     <tr>
 *         <th>Name</th>
 *         <th>Default</th>
 *         <th>Description</th>
 *     </tr>
 *     <tr>
 *         <td>logger</td>
 *         <td>(None)</td>
 *         <td>The SLF4J {@link Logger} to use. This is required.</td>
 *     </tr>
 *     <tr>
 *         <td>initialTimestamp</td>
 *         <td>0</td>
 *         <td>
 *             The nanoseconds to use as the starting point against which the next elapsed time should be
 *             measured, e.g., using {@link System#nanoTime()}. When this is zero, the first elapsed log message
 *             will be the {@code initialMessage} to indicate there is not a previous timestamp against which
 *             to measure. If you want to start measurement from the time a {@link TimestampingLogger} is
 *             created, set it to {@link System#nanoTime()}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>elapsedTimeTemplate</td>
 *         <td>"[elapsed time since previous: {} nanoseconds / {} millis]"</td>
 *         <td>
 *             The template to use when logging elapsed time messages.
 *             Uses {@link KiwiStrings#format(String, Object...)} to format messages.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>argumentTransformer</td>
 *         <td>{@code new Object[] { elapsedNanos, elapsedMillis }}</td>
 *         <td>
 *             A {@link BiFunction} that accepts nanoseconds and the log count, and which should convert those
 *             into arguments for the {@code elapsedTimeTemplate}. If you customize the {@code elapsedTimeTemplate}
 *             then this should return an array with the same number of elements as there as placeholders in the
 *             template. For example, the default template has two placeholders, for nanos and millis, and the
 *             default transformer returns an array with two elements, the elapsed nanos and millis. The log count
 *             is simply the number of times the elapsed time has been logged. It is not used by the default
 *             {@code elapsedTimeTemplate}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>skipInitialMessage</td>
 *         <td>false</td>
 *         <td>
 *             When true, no elapsed time message is logged the first time an elapsed time is logged. By default,
 *             an initial message will be printed, unless an {@code initialTimestamp} is supplied. In that case,
 *             the first elapsed time message will use the {@code elapsedTimeTemplate}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>initialMessage</td>
 *         <td>"[elapsed time since previous: N/A (no previous timestamp)]"</td>
 *         <td>
 *             The message to log the first time elapsed time is logged, assuming the previous timestamp is zero.
 *         </td>
 *     </tr>
 * </table>
 * <p>
 * <em>Currently, this is intended only to be used within a single thread.</em>
 */
@Beta
public class TimestampingLogger {

    @SuppressWarnings("NonConstantLogger")
    private final Logger logger;
    private final String elapsedTimeTemplate;
    private final BiFunction<Long, Integer, Object[]> argumentTransformer;
    private final String initialMessage;

    // Fields whose values may change
    private long previousTimestamp;
    private int logCount;

    /**
     * Create a new instance with default values using the given {@link Logger}.
     */
    public TimestampingLogger(Logger logger) {
        this(logger, 0, null, null, false, null);
    }

    /**
     * Create a new instance.
     *
     * @param logger the {@link Logger} to use when logging
     * @param initialTimestamp allows setting an initial value against which elapsed time should be measured
     * @param elapsedTimeTemplate the message template to use when logging elapsed time
     * @param argumentTransformer a function that transforms the elapsed nanoseconds and log count into template arguments
     * @param skipInitialMessage whether to skip logging the first time the elapsed time is logged
     * @param initialMessage the message to log the first time the elapsed time is logged
     */
    @Builder
    TimestampingLogger(Logger logger,
                       long initialTimestamp,
                       String elapsedTimeTemplate,
                       BiFunction<Long, Integer, Object[]> argumentTransformer,
                       boolean skipInitialMessage,
                       String initialMessage) {

        this.logger = requireNotNull(logger);
        this.previousTimestamp = requirePositiveOrZero(initialTimestamp);
        this.logCount = 0;

        this.elapsedTimeTemplate = isBlank(elapsedTimeTemplate) ?
                "[elapsed time since previous: {} nanoseconds / {} millis]" : elapsedTimeTemplate;

        if (isNull(argumentTransformer)) {
            this.argumentTransformer = (nanos, count) -> new Object[] { nanos, nanosToMillis(nanos)};
        } else {
            this.argumentTransformer = argumentTransformer;
        }

        if (skipInitialMessage) {
            this.initialMessage = null;
        } else {
            this.initialMessage = isBlank(initialMessage) ?
                    "[elapsed time since previous: N/A (no previous timestamp)]" : initialMessage;
        }
    }

    private static long nanosToMillis(long diffInNanos) {
        return Duration.ofNanos(diffInNanos).toMillis();
    }

    /**
     * Logs a message and an elapsed time message at TRACE level.
     *
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     * @see #logElapsed(Level, String, Object...)
     */
    public void traceLogElapsed(String message, Object... args) {
        logElapsed(Level.TRACE, message, args);
    }

    /**
     * Logs a message and an elapsed time message at DEBUG level.
     *
     * @param message the message or message template
     * @param args    the arguments to the message template, if any
     * @see #logElapsed(Level, String, Object...)
     */
    public void debugLogElapsed(String message, Object... args) {
        logElapsed(Level.DEBUG, message, args);
    }

    /**
     * Logs the given message and then logs the elapsed time since the previous log.
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
            logElapsedSincePreviousTimestamp(level, now);
            previousTimestamp = now;
            ++logCount;
        }
    }

    private void logElapsedSincePreviousTimestamp(Level level, long now) {
        if (previousTimestamp > 0) {
            var diffInNanos = now - previousTimestamp;
            var args = argumentTransformer.apply(diffInNanos, logCount);
            KiwiSlf4j.log(logger, level, elapsedTimeTemplate, args);
        } else if (isNotBlank(initialMessage)) {
            KiwiSlf4j.log(logger, level, initialMessage);
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
            logAppendingElapsedSincePreviousTimestamp(level, formattedMessage, now);
            previousTimestamp = now;
            ++logCount;
        }
    }

    private void logAppendingElapsedSincePreviousTimestamp(Level level, String formattedMessage, long now) {
        if (previousTimestamp > 0) {
            var diffInNanos = now - previousTimestamp;
            var args = argumentTransformer.apply(diffInNanos, logCount);
            var elapsedTimeMessage = KiwiStrings.f(elapsedTimeTemplate, args);
            KiwiSlf4j.log(logger, level, formattedMessage + " " + elapsedTimeMessage);
        } else if (isBlank(initialMessage)) {
            KiwiSlf4j.log(logger, level, formattedMessage);
        } else {
            KiwiSlf4j.log(logger, level, formattedMessage + " " + initialMessage);
        }
    }
}
