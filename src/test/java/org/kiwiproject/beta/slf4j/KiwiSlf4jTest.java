package org.kiwiproject.beta.slf4j;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.collect.KiwiLists.first;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Enum;
import org.kiwiproject.test.junit.jupiter.params.provider.MinimalBlankStringSource;
import org.kiwiproject.test.logback.InMemoryAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.IOException;
import java.util.Locale;

/**
 * @implNote See the loggers defined for this test in {@code src/test/resources/logback-test.xml}.
 */
@DisplayName("KiwiSlf4j")
class KiwiSlf4jTest {

    private static final String TEST_CLASS_NAME = KiwiSlf4jTest.class.getName();

    private InMemoryAppender appender;

    @BeforeEach
    void setUp() {
        // We can get the in-memory appender from any of the loggers defined for this test
        var logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TEST_CLASS_NAME + ".DEBUG");
        appender = (InMemoryAppender) logger.getAppender("MEMORY");
    }

    @AfterEach
    void tearDown() {
        appender.clearEvents();
    }

    @CartesianTest(name = "[{index}] logger: {0} check: {1}")
    void shouldCheckIsEnabled_ForLevelName(@Enum(Level.class) Level loggerLevel,
                                           @Enum(Level.class) Level checkLevel) {

        var logger = getLoggerAtLevel(loggerLevel);
        var enabled = KiwiSlf4j.isEnabled(logger, checkLevel.name());
        var expected = expectLogEvent(loggerLevel, checkLevel);
        assertThat(enabled).isEqualTo(expected);
    }

    @CartesianTest(name = "[{index}] logger: {0} check: {1}")
    void shouldCheckIsEnabled_ForLevel(@Enum(Level.class) Level loggerLevel,
                                       @Enum(Level.class) Level checkLevel) {

        var logger = getLoggerAtLevel(loggerLevel);
        var enabled = KiwiSlf4j.isEnabled(logger, checkLevel);
        var expected = expectLogEvent(loggerLevel, checkLevel);
        assertThat(enabled).isEqualTo(expected);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessage_ForLevelName(@Enum(Level.class) Level loggerLevel,
                                       @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        var message = "a message";
        KiwiSlf4j.log(logger, logAtLevel.name(), message);

        assertLoggingEvent(loggerLevel, logAtLevel, message);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessage_ForLevel(@Enum(Level.class) Level loggerLevel,
                                   @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        var message = "a message";
        KiwiSlf4j.log(logger, logAtLevel, message);

        assertLoggingEvent(loggerLevel, logAtLevel, message);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithArgument_ForLevelName(@Enum(Level.class) Level loggerLevel,
                                                   @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel.name(), "a message with one arg: {}", 42);

        var expectedMessage = "a message with one arg: 42";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithArgument_ForLevel(@Enum(Level.class) Level loggerLevel,
                                               @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel, "a message with one arg: {}", 42);

        var expectedMessage = "a message with one arg: 42";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithTwoArguments_ForLevelName(@Enum(Level.class) Level loggerLevel,
                                                       @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel.name(), "a message with two args: {}, {}", 42, "foo");

        var expectedMessage = "a message with two args: 42, foo";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithTwoArguments_ForLevel(@Enum(Level.class) Level loggerLevel,
                                                   @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel, "a message with two args: {}, {}", 42, "foo");

        var expectedMessage = "a message with two args: 42, foo";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithVariableArguments_ForLevelName(@Enum(Level.class) Level loggerLevel,
                                                            @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel.name(), "a message with varargs: {}, {}, {}, and {}", 42, "foo", "bar", 84.0);

        var expectedMessage = "a message with varargs: 42, foo, bar, and 84.0";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithVariableArguments_ForLevel(@Enum(Level.class) Level loggerLevel,
                                                        @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel, "a message with varargs: {}, {}, {}, and {}", 42, "foo", "bar", 84.0);

        var expectedMessage = "a message with varargs: 42, foo, bar, and 84.0";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @Test
    void shouldLogMessageWithVariableArgumentsAndThrowable() {
        var level = Level.WARN;
        var logger = getLoggerAtLevel(level);
        var ex = new IOException("i/o failure");

        KiwiSlf4j.log(logger, level, "a message with several args and a Throwable: {}, {}, {}, and {}",
                "foo", 42, "bar", 84.0, ex);

        var expectedMessage = "a message with several args and a Throwable: foo, 42, bar, and 84.0";

        assertLoggingEvent(level, level, expectedMessage, ex);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithThrowable_ForLevelName(@Enum(Level.class) Level loggerLevel,
                                                    @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        var message = "an error message";
        var t = new IOException("I/O problem");
        KiwiSlf4j.log(logger, logAtLevel.name(), message, t);

        assertLoggingEvent(loggerLevel, logAtLevel, message, t);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithThrowable_ForLevel(@Enum(Level.class) Level loggerLevel,
                                                @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        var message = "an error message";
        var t = new IOException("I/O problem");
        KiwiSlf4j.log(logger, logAtLevel, message, t);

        assertLoggingEvent(loggerLevel, logAtLevel, message, t);
    }

    private static Logger getLoggerAtLevel(Level loggerLevel) {
        return LoggerFactory.getLogger(TEST_CLASS_NAME + "." + loggerLevel.name());
    }

    private void assertLoggingEvent(Level loggerLevel, Level logAtLevel, String expectedMessage) {
        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage, null);
    }

    private void assertLoggingEvent(Level loggerLevel,
                                    Level logAtLevel,
                                    String expectedMessage,
                                    @Nullable Throwable expectedThrowable) {

        if (expectLogEvent(loggerLevel, logAtLevel)) {
            assertLoggingEvent(logAtLevel, expectedMessage, expectedThrowable);
        } else {
            assertThat(appender.orderedEvents()).isEmpty();
        }
    }

    private void assertLoggingEvent(Level logAtLevel, String expectedMessage, @Nullable Throwable expectedThrowable) {
        assertThat(appender.orderedEvents()).hasSize(1);
        var event = first(appender.orderedEvents());

        assertThat(event.getLevel()).hasToString(logAtLevel.toString());
        assertThat(event.getFormattedMessage()).isEqualTo(expectedMessage);

        if (nonNull(expectedThrowable)) {
            assertThat(event.getThrowableProxy().getClassName()).isEqualTo(expectedThrowable.getClass().getName());
            assertThat(event.getThrowableProxy().getMessage()).isEqualTo(expectedThrowable.getMessage());
        }
    }

    private boolean expectLogEvent(Level loggerLevel, Level logAtLevel) {
        return loggerLevel.toInt() <= logAtLevel.toInt();
    }

    @ParameterizedTest
    @MinimalBlankStringSource
    void toLevelIgnoreCase_shouldRequireLevelName(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiSlf4j.toLevelIgnoreCase(value))
                .withMessage("levelNameString must not be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "DEBU",
        "DEBUGGING",
        "information",
        "WARNING",
        "warn_",
        "_warn_",
        "FATAL"
    })
    void toLevelIgnoreCase_shouldThrowIllegalArgumentException_WhenNoneMatch(String value) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiSlf4j.toLevelIgnoreCase(value))
                .withMessage("No enum constant org.slf4j.event.Level." + value.toUpperCase(Locale.ENGLISH));
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            TRACE, TRACE
            trace, TRACE
            Trace, TRACE
            TrAcE, TRACE
            DEBUG, DEBUG
            debug, DEBUG
            Debug, DEBUG
            DeBuG, DEBUG
            INFO, INFO
            info, INFO
            Info, INFO
            InFO, INFO
            WARN, WARN
            warn, WARN
            Warn, WARN
            WArn, WARN
            ERROR, ERROR
            error, ERROR
            Error, ERROR
            ERRoR, ERROR
            """)
    void toLevelIgnoreCase_shouldGetLevel_IgnoringCase(String levelName, Level expectedLevel) {
        var level = KiwiSlf4j.toLevelIgnoreCase(levelName);
        assertThat(level).isSameAs(expectedLevel);
    }
}
