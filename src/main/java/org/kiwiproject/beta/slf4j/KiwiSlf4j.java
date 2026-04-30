package org.kiwiproject.beta.slf4j;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.kiwiproject.base.KiwiDeprecated;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.Locale;

/**
 * Utilities for working with SLF4J {@link Logger} instances and {@link Level} values.
 * <p>
 * Sometimes it is useful to select a logging level dynamically instead of hard-coding
 * calls such as {@code LOG.debug(...)} or {@code LOG.info(...)}. This class provides
 * helpers for checking whether a logger is enabled for a dynamically selected level,
 * and for converting level names to SLF4J {@link Level} values in a case-insensitive way.
 * <p>
 * The {@code log} methods are deprecated since SLF4J 2 provides equivalent functionality
 * via its fluent logging API, e.g. {@code logger.atLevel(level).log(message)}.
 */
@UtilityClass
@Beta
public class KiwiSlf4j {

    /**
     * Returns whether the given logger is enabled for the level represented by the given level name.
     * <p>
     * The level name is matched case-insensitively using {@link #toLevelIgnoreCase(String)}.
     *
     * @param logger the SLF4J logger to check
     * @param levelName the name of the SLF4J level to check
     * @return true if the logger is enabled for the matching level; otherwise false
     * @throws IllegalArgumentException if {@code levelName} is blank or there is no matching {@link Level}
     */
    public static boolean isEnabled(Logger logger, String levelName) {
        return isEnabled(logger, toLevelIgnoreCase(levelName));
    }

    /**
     * Returns whether the given logger is enabled for the given SLF4J level.
     *
     * @param logger the SLF4J logger to check
     * @param level the SLF4J level to check
     * @return true if the logger is enabled for the given level; otherwise false
     */
    public static boolean isEnabled(Logger logger, Level level) {
        return switch (level) {
            case ERROR -> logger.isErrorEnabled();
            case WARN -> logger.isWarnEnabled();
            case INFO -> logger.isInfoEnabled();
            case DEBUG -> logger.isDebugEnabled();
            case TRACE -> logger.isTraceEnabled();
        };
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(message)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(message)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static void log(Logger logger, String levelName, String message) {
        log(logger, toLevelIgnoreCase(levelName), message);
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(level).log(message)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(level).log(message)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static void log(Logger logger, Level level, String message) {
        switch (level) {
            case ERROR -> logger.error(message);
            case WARN -> logger.warn(message);
            case INFO -> logger.info(message);
            case DEBUG -> logger.debug(message);
            case TRACE -> logger.trace(message);
        }
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(format, arg)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(format, arg)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static void log(Logger logger, String levelName, String format, Object arg) {
        log(logger, toLevelIgnoreCase(levelName), format, arg);
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(level).log(format, arg)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(level).log(format, arg)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
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

    /**
     * @deprecated replaced by {@code logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(format, arg1, arg2)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(format, arg1, arg2)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static void log(Logger logger, String levelName, String format, Object arg1, Object arg2) {
        log(logger, toLevelIgnoreCase(levelName), format, arg1, arg2);
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(level).log(format, arg1, arg2)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(level).log(format, arg1, arg2)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static void log(Logger logger, Level level, String format, Object arg1, Object arg2) {
        switch (level) {
            case ERROR -> logger.error(format, arg1, arg2);
            case WARN -> logger.warn(format, arg1, arg2);
            case INFO -> logger.info(format, arg1, arg2);
            case DEBUG -> logger.debug(format, arg1, arg2);
            case TRACE -> logger.trace(format, arg1, arg2);
        }
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(format, arguments)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).log(format, arguments)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static void log(Logger logger, String levelName, String format, Object... arguments) {
        log(logger, toLevelIgnoreCase(levelName), format, arguments);
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(level).log(format, arguments)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(level).log(format, arguments)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
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

    /**
     * @deprecated replaced by {@code logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).setCause(t).log(message)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(KiwiSlf4j.toLevelIgnoreCase(levelName)).setCause(t).log(message)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
    public static void log(Logger logger, String levelName, String message, Throwable t) {
        log(logger, toLevelIgnoreCase(levelName), message, t);
    }

    /**
     * @deprecated replaced by {@code logger.atLevel(level).setCause(t).log(message)}
     */
    @KiwiDeprecated(
        removeAt = "4.0.0",
        replacedBy = "logger.atLevel(level).setCause(t).log(message)",
        reference = "https://github.com/sleberknight/kiwi-beta/issues/613"
    )
    @Deprecated(since = "3.1.0", forRemoval = true)
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
     * Matches {@code levelNameString} to a value in {@link Level},
     * independent of the character case (lower or upper).
     * <p>
     * For example, the strings "WARN", "warn", and "Warn" all match {@link Level#WARN}.
     * 
     * @param levelNameString a String whose uppercased value maps to one of the values in {@code Level}
     * @return the matching SLF4J {@code Level}
     * @throws IllegalArgumentException if {@code levelNameString} is blank or there is no matching {@code Level}
     */
    public static Level toLevelIgnoreCase(String levelNameString) {
        checkArgumentNotBlank(levelNameString, "levelNameString must not be blank");
        return Level.valueOf(levelNameString.toUpperCase(Locale.ENGLISH));
    }
}
