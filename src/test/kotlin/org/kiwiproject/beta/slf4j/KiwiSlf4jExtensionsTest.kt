package org.kiwiproject.beta.slf4j

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junitpioneer.jupiter.cartesian.CartesianTest
import org.kiwiproject.beta.test.logback.InMemoryAppender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.io.IOException

//
// See the loggers (which must exist) for this test in src/test/resources/logback-test.xml
//
@DisplayName("KiwiSlf4jExtensions")
internal class KiwiSlf4jExtensionsTest {

    private val testClassName = KiwiSlf4jExtensionsTest::class.qualifiedName

    private lateinit var appender: InMemoryAppender

    @BeforeEach
    fun setUp() {
        // We can get the in-memory appender from any of the loggers defined for this test
        val logger = LoggerFactory.getLogger("$testClassName.DEBUG") as ch.qos.logback.classic.Logger
        appender = logger.getAppender("MEMORY") as InMemoryAppender
    }

    @AfterEach
    fun tearDown() {
        appender.clearEvents()
    }

    @CartesianTest(name = "[{index}] logger: {0} check: {1}")
    fun `should check isEnabled`(
        @CartesianTest.Enum(Level::class) loggerLevel: Level,
        @CartesianTest.Enum(Level::class) checkLevel: Level
    ) {
        val logger = getLoggerAtLevel(loggerLevel)
        val enabled = logger.isEnabled(checkLevel)

        val expected = KiwiSlf4j.isEnabled(logger, checkLevel)

        assertThat(enabled).isEqualTo(expected)
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    fun `should log message`(
        @CartesianTest.Enum(Level::class) loggerLevel: Level,
        @CartesianTest.Enum(Level::class) logAtLevel: Level
    ) {
        val logger = getLoggerAtLevel(loggerLevel)

        val message = "a message"

        KiwiSlf4j.log(logger, logAtLevel, message)
        val expectedMessages = appender.orderedEventMessages
        appender.clearEvents()

        logger.log(logAtLevel, message)
        val actualMessages = appender.orderedEventMessages
        assertThat(actualMessages).isEqualTo(expectedMessages)
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    fun `should log message with one argument`(
        @CartesianTest.Enum(Level::class) loggerLevel: Level,
        @CartesianTest.Enum(Level::class) logAtLevel: Level
    ) {
        val logger = getLoggerAtLevel(loggerLevel)

        val format = "a message with one arg: {}"
        val arg = 42
        KiwiSlf4j.log(logger, logAtLevel, format, arg)
        val expectedMessages = appender.orderedEventMessages
        appender.clearEvents()

        logger.log(logAtLevel, format, arg)
        val actualMessages = appender.orderedEventMessages
        assertThat(actualMessages).isEqualTo(expectedMessages)
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    fun `should log message with two arguments`(
        @CartesianTest.Enum(Level::class) loggerLevel: Level,
        @CartesianTest.Enum(Level::class) logAtLevel: Level
    ) {
        val logger = getLoggerAtLevel(loggerLevel)

        val format = "a message with two args: {}, {}"
        val arg1 = 42
        val arg2 = "foo"
        KiwiSlf4j.log(logger, logAtLevel, format, arg1, arg2)
        val expectedMessages = appender.orderedEventMessages
        appender.clearEvents()

        logger.log(logAtLevel, format, arg1, arg2)
        val actualMessages = appender.orderedEventMessages
        assertThat(actualMessages).isEqualTo(expectedMessages)
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    fun `should log message with variable arguments`(
        @CartesianTest.Enum(Level::class) loggerLevel: Level,
        @CartesianTest.Enum(Level::class) logAtLevel: Level
    ) {
        val logger = getLoggerAtLevel(loggerLevel)

        val format = "a message with varargs: {}, {}, {}, and {}"
        val arg1 = 42
        val arg2 = "foo"
        val arg3 = "bar"
        val arg4 = 84.0
        KiwiSlf4j.log(logger, logAtLevel, format, arg1, arg2, arg3, arg4)

        val expectedMessages = appender.orderedEventMessages
        appender.clearEvents()

        logger.log(logAtLevel, format, arg1, arg2, arg3, arg4)
        val actualMessages = appender.orderedEventMessages
        assertThat(actualMessages).isEqualTo(expectedMessages)
    }

    @CartesianTest(name = "[{index}] logger: {0} logAt: {1}")
    fun `should log message with Throwable`(
        @CartesianTest.Enum(Level::class) loggerLevel: Level,
        @CartesianTest.Enum(Level::class) logAtLevel: Level
    ) {
        val logger = getLoggerAtLevel(loggerLevel)

        val message = "an error message"
        val t = IOException("I/O problem")
        KiwiSlf4j.log(logger, logAtLevel, message, t)
        val expectedMessages = appender.orderedEventMessages
        appender.clearEvents()

        logger.log(logAtLevel, message, t)
        val actualMessages = appender.orderedEventMessages
        assertThat(actualMessages).isEqualTo(expectedMessages)
    }

    @Test
    fun `should call various logging methods`() {
        val logger = getLoggerAtLevel(Level.DEBUG)

        logger.log(Level.DEBUG, "the message")

        logger.log(Level.DEBUG, "message with arg: {}", 42)
        logger.log(Level.DEBUG, "message with arg: {}", "the arg")
        logger.log(Level.DEBUG, "message with arg: {}", Person("Bob", "Smith"))

        logger.log(Level.DEBUG, "message with arg1: {} and arg2: {}", 42, "foo")
        logger.log(Level.DEBUG, "message with arg1: {} and arg2: {}", Person("Alice", "Jones"), "foo")

        logger.log(
            Level.DEBUG, "an error message with arg {} and an exception",
            42, RuntimeException("oops")
        )

        logger.log(
            Level.DEBUG, "message with arg1: {} and arg2: {} and arg3: {}",
            42, "foo", 84
        )
        logger.log(
            Level.DEBUG, "message with arg1: {} and arg2: {} and arg3: {} and arg4: {}",
            42, "foo", 84, Person("Carlos", "Fernandez")
        )
        logger.log(
            Level.DEBUG, "message with arg1: {} and arg2: {} and arg3: {} and arg4: {} and an exception",
            42, "foo", 84, Person("Diane", "Mandala"), RuntimeException("another oop")
        )

        logger.log(Level.DEBUG, "an error message", RuntimeException("oops again"))

        assertThat(appender.orderedEventMessages).containsExactly(
            "the message",
            "message with arg: 42",
            "message with arg: the arg",
            "message with arg: Bob Smith",
            "message with arg1: 42 and arg2: foo",
            "message with arg1: Alice Jones and arg2: foo",
            "an error message with arg 42 and an exception",
            "message with arg1: 42 and arg2: foo and arg3: 84",
            "message with arg1: 42 and arg2: foo and arg3: 84 and arg4: Carlos Fernandez",
            "message with arg1: 42 and arg2: foo and arg3: 84 and arg4: Diane Mandala and an exception",
            "an error message"
        )

        val throwableMessages = appender.orderedEvents
            .filter { it.throwableProxy != null }
            .map { it.throwableProxy.message }
            .toList()
        assertThat(throwableMessages).containsExactly(
            "oops",
            "another oop",
            "oops again"
        )
    }

    data class Person(val firstName: String, val lastName: String) {
        override fun toString(): String {
            return "$firstName $lastName"
        }
    }

    private fun getLoggerAtLevel(loggerLevel: Level): Logger =
        LoggerFactory.getLogger("$testClassName.${loggerLevel.name}")

}
