package org.kiwiproject.beta.slf4j;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Locale;

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

    public static boolean isEnabled(Logger logger, String levelName) {
        return isEnabled(logger, levelFromStringIgnoreCase(levelName));
    }

    public static boolean isEnabled(Logger logger, Level level) {
        return switch (level) {
            case ERROR -> logger.isErrorEnabled();
            case WARN -> logger.isWarnEnabled();
            case INFO -> logger.isInfoEnabled();
            case DEBUG -> logger.isDebugEnabled();
            case TRACE -> logger.isTraceEnabled();
        };
    }

    public static void log(Logger logger, String levelName, String message) {
        log(logger, levelFromStringIgnoreCase(levelName), message);
    }

    public static void log(Logger logger, Level level, String message) {
        switch (level) {
            case ERROR -> logger.error(message);
            case WARN -> logger.warn(message);
            case INFO -> logger.info(message);
            case DEBUG -> logger.debug(message);
            case TRACE -> logger.trace(message);
        }
    }

    public static void log(Logger logger, String levelName, String format, Object arg) {
        log(logger, levelFromStringIgnoreCase(levelName), format, arg);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void log(Logger logger, Level level, String format, Object arg) {
        switch (level) {
            case ERROR -> logger.error(format, arg);
            case WARN -> logger.warn(format, arg);
            case INFO -> logger.info(format, arg);
            case DEBUG -> logger.debug(format, arg);
            case TRACE -> logger.trace(format, arg);
        }
    }

    public static void log(Logger logger, String levelName, String format, Object arg1, Object arg2) {
        log(logger, levelFromStringIgnoreCase(levelName), format, arg1, arg2);
    }

    public static void log(Logger logger, Level level, String format, Object arg1, Object arg2) {
        switch (level) {
            case ERROR -> logger.error(format, arg1, arg2);
            case WARN -> logger.warn(format, arg1, arg2);
            case INFO -> logger.info(format, arg1, arg2);
            case DEBUG -> logger.debug(format, arg1, arg2);
            case TRACE -> logger.trace(format, arg1, arg2);
        }
    }

    public static void log(Logger logger, String levelName, String format, Object... arguments) {
        log(logger, levelFromStringIgnoreCase(levelName), format, arguments);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void log(Logger logger, Level level, String format, Object... arguments) {
        switch (level) {
            case ERROR -> logger.error(format, arguments);
            case WARN -> logger.warn(format, arguments);
            case INFO -> logger.info(format, arguments);
            case DEBUG -> logger.debug(format, arguments);
            case TRACE -> logger.trace(format, arguments);
        }
    }

    public static void log(Logger logger, String levelName, String message, Throwable t) {
        log(logger, levelFromStringIgnoreCase(levelName), message, t);
    }

    @SuppressWarnings("DuplicatedCode")
    public static void log(Logger logger, Level level, String message, Throwable t) {
        switch (level) {
            case ERROR -> logger.error(message, t);
            case WARN -> logger.warn(message, t);
            case INFO -> logger.info(message, t);
            case DEBUG -> logger.debug(message, t);
            case TRACE -> logger.trace(message, t);
        }
    }

    /**
     * Matches the given {@code levelNameString} to a value in {@link Level},
     * independent of the character case (lower or upper).
     * <p>
     * For example, the strings "WARN", "warn", and "Warn" all match {@link Level#WARN}.
     * 
     * @param levelNameString a String whose uppercased value maps to one of the values in {@code Level}
     * @return the matching SLF4J {@code Level}
     * @throws IllegalArgumentException if there is no matching {@code Level}
     */
    public static Level levelFromStringIgnoreCase(String levelNameString) {
        checkArgumentNotBlank(levelNameString, "levelNameString must not be blank");
        return Level.valueOf(levelNameString.toUpperCase(Locale.ENGLISH));
    }
}
