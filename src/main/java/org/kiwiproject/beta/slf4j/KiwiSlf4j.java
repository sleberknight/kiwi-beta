package org.kiwiproject.beta.slf4j;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.event.Level;

/**
 * Utilities for SLF4J, mainly to allow logging a message at a specified level. Sometimes it is useful to be able
 * not just to change the log level of loggers at runtime, but also to select the level of specific log statements.
 * For example, instead of having to hard code a {@code LOG.debug(...)}, you can use one of the {@code log} methods
 * in this utility to permit changing the level at which a specific statement is logged. This is more granular than
 * just changing an entire Logger's level, and is extremely useful in some situations.
 */
@UtilityClass
@Beta
public class KiwiSlf4j {

    public static boolean isEnabled(Logger logger, Level level) {
        switch (level) {
            case ERROR:
                return logger.isErrorEnabled();

            case WARN:
                return logger.isWarnEnabled();

            case INFO:
                return logger.isInfoEnabled();

            case DEBUG:
                return logger.isDebugEnabled();

            case TRACE:
                return logger.isTraceEnabled();
        }

        // Won't need the following nonsense as of JDK 14 and higher; can then use the exhaustive switch expression.
        // See: https://openjdk.java.net/jeps/361

        throw new IllegalStateException("Unhandled Level: " + level);
    }

    public static void log(Logger logger, Level level, String message) {
        switch (level) {
            case ERROR:
                logger.error(message);
                break;

            case WARN:
                logger.warn(message);
                break;

            case INFO:
                logger.info(message);
                break;

            case DEBUG:
                logger.debug(message);
                break;

            case TRACE:
                logger.trace(message);
                break;
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static void log(Logger logger, Level level, String format, Object arg) {
        switch (level) {
            case ERROR:
                logger.error(format, arg);
                break;

            case WARN:
                logger.warn(format, arg);
                break;

            case INFO:
                logger.info(format, arg);
                break;

            case DEBUG:
                logger.debug(format, arg);
                break;

            case TRACE:
                logger.trace(format, arg);
                break;
        }
    }

    public static void log(Logger logger, Level level, String format, Object arg1, Object arg2) {
        switch (level) {
            case ERROR:
                logger.error(format, arg1, arg2);
                break;

            case WARN:
                logger.warn(format, arg1, arg2);
                break;

            case INFO:
                logger.info(format, arg1, arg2);
                break;

            case DEBUG:
                logger.debug(format, arg1, arg2);
                break;

            case TRACE:
                logger.trace(format, arg1, arg2);
                break;
        }
    }

    @SuppressWarnings("DuplicatedCode")
    public static void log(Logger logger, Level level, String format, Object... arguments) {
        switch (level) {
            case ERROR:
                logger.error(format, arguments);
                break;

            case WARN:
                logger.warn(format, arguments);
                break;

            case INFO:
                logger.info(format, arguments);
                break;

            case DEBUG:
                logger.debug(format, arguments);
                break;

            case TRACE:
                logger.trace(format, arguments);
                break;
        }

    }

    @SuppressWarnings("DuplicatedCode")
    public static void log(Logger logger, Level level, String message, Throwable t) {
        switch (level) {
            case ERROR:
                logger.error(message, t);
                break;

            case WARN:
                logger.warn(message, t);
                break;

            case INFO:
                logger.info(message, t);
                break;

            case DEBUG:
                logger.debug(message, t);
                break;

            case TRACE:
                logger.trace(message, t);
                break;
        }
    }
}
