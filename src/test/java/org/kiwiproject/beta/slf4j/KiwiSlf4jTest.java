package org.kiwiproject.beta.slf4j;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiLists.first;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junitpioneer.jupiter.cartesian.CartesianTest;
import org.junitpioneer.jupiter.cartesian.CartesianTest.Enum;
import org.kiwiproject.beta.test.logback.InMemoryAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * @implNote See the loggers defined for this test in {@code src/test/resources/logback.xml}.
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
    void shouldCheckIsEnabled(@Enum(Level.class) Level loggerLevel,
                              @Enum(Level.class) Level checkLevel) {

        var logger = getLoggerAtLevel(loggerLevel);
        var enabled = KiwiSlf4j.isEnabled(logger, checkLevel);
        var expected = expectLogEvent(loggerLevel, checkLevel);
        assertThat(enabled).isEqualTo(expected);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessage(@Enum(Level.class) Level loggerLevel,
                          @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        var message = "a message";
        KiwiSlf4j.log(logger, logAtLevel, message);

        assertLoggingEvent(loggerLevel, logAtLevel, message);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithArgument(@Enum(Level.class) Level loggerLevel,
                                      @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel, "a message with one arg: {}", 42);

        var expectedMessage = "a message with one arg: 42";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithTwoArguments(@Enum(Level.class) Level loggerLevel,
                                          @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel, "a message with two args: {}, {}", 42, "foo");

        var expectedMessage = "a message with two args: 42, foo";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithVariableArguments(@Enum(Level.class) Level loggerLevel,
                                               @Enum(Level.class) Level logAtLevel) {

        var logger = getLoggerAtLevel(loggerLevel);

        KiwiSlf4j.log(logger, logAtLevel, "a message with varargs: {}, {}, {}, and {}", 42, "foo", "bar", 84.0);

        var expectedMessage = "a message with varargs: 42, foo, bar, and 84.0";

        assertLoggingEvent(loggerLevel, logAtLevel, expectedMessage);
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    void shouldLogMessageWithThrowable(@Enum(Level.class) Level loggerLevel,
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
            assertThat(appender.getOrderedEvents()).isEmpty();
        }
    }

    private void assertLoggingEvent(Level logAtLevel, String expectedMessage, @Nullable Throwable expectedThrowable) {
        assertThat(appender.getOrderedEvents()).hasSize(1);
        var event = first(appender.getOrderedEvents());

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
}