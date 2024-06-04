package org.kiwiproject.beta.slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Index.atIndex;
import static org.kiwiproject.base.KiwiPreconditions.checkEvenItemCount;
import static org.kiwiproject.collect.KiwiLists.fifth;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.fourth;
import static org.kiwiproject.collect.KiwiLists.last;
import static org.kiwiproject.collect.KiwiLists.second;
import static org.kiwiproject.collect.KiwiLists.subListExcludingFirst;
import static org.kiwiproject.collect.KiwiLists.third;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.beta.test.logback.InMemoryAppender;
import org.kiwiproject.collect.KiwiLists;
import org.kiwiproject.time.KiwiDurationFormatters;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("removal")
@DisplayName("TimestampingLogger")
class TimestampingLoggerTest {

    private static Logger logbackLogger;
    private static InMemoryAppender appender;

    private TimestampingLogger timestampingLogger;
    private List<String> messages;

    @BeforeAll
    static void beforeAll() {
        logbackLogger = (Logger) LoggerFactory.getLogger(TimestampingLoggerTest.class);
        resetLevelToTrace(logbackLogger);

        var context = (LoggerContext) LoggerFactory.getILoggerFactory();

        appender = new InMemoryAppender();
        appender.setContext(context);
        appender.start();

        logbackLogger.addAppender(appender);
    }

    @BeforeEach
    void setUp() {
        timestampingLogger = new TimestampingLogger(logbackLogger);

        messages = List.of("At time 0", "At time 1", "At time 2", "At time 3", "At time 4");
    }

    @AfterEach
    void tearDown() {
        appender.clearEvents();
        resetLevelToTrace(logbackLogger);
    }

    private static void resetLevelToTrace(Logger logger) {
        logger.setLevel(ch.qos.logback.classic.Level.TRACE);
    }

    @AfterAll
    static void afterAll() {
        appender.stop();
        logbackLogger.detachAppender(appender);
    }

    @Test
    void shouldLogElapsedTimeWithArguments() {
        timestampingLogger.logElapsed(Level.DEBUG, "{} created with id {}", "User", 42);
        timestampingLogger.logElapsed(Level.DEBUG, "{} updated with id {}", "Order", 336);

        List<String> eventMessages = appender.getOrderedEventMessages();

        assertThat(eventMessages).hasSize(4);
        assertThat(first(eventMessages)).isEqualTo("User created with id 42");
        assertThat(second(eventMessages)).isEqualTo("[elapsed time since previous: N/A (no previous timestamp)]");
        assertThat(third(eventMessages)).isEqualTo("Order updated with id 336");
        assertThat(fourth(eventMessages)).startsWith("[elapsed time since previous: ")
                .contains(" nanoseconds /")
                .endsWith(" millis]");
    }

    @Test
    void shouldNotLogElapsedTimeWhenLevelIsInactive() {
        logbackLogger.setLevel(ch.qos.logback.classic.Level.WARN);

        timestampingLogger.logElapsed(Level.INFO, "Should not see this!");
        assertThat(appender.getOrderedEventMessages()).isEmpty();
    }

    @Test
    void shouldTraceLogElapsedTime() {
        messages.forEach(message -> timestampingLogger.traceLogElapsed(message));
        assertElapsedEventMessages(Level.TRACE);
    }

    @Test
    void shouldDebugLogElapsedTime() {
        messages.forEach(message -> timestampingLogger.debugLogElapsed(message));
        assertElapsedEventMessages(Level.DEBUG);
    }

    @Test
    void shouldLogElapsedTime() {
        messages.forEach(message -> timestampingLogger.logElapsed(Level.INFO, message));
        assertElapsedEventMessages(Level.INFO);
    }

    private void assertElapsedEventMessages(Level expectedLevel) {
        List<ILoggingEvent> orderedEvents = appender.getOrderedEvents();
        var expectedLogbackLevel = ch.qos.logback.classic.Level.convertAnSLF4JLevel(expectedLevel);
        assertThat(orderedEvents)
                .describedAs("All events should have level %s", expectedLevel)
                .isNotEmpty()
                .allMatch(event -> event.getLevel() == expectedLogbackLevel);

        List<String> eventMessages = appender.getOrderedEventMessages();
        assertThat(eventMessages)
                .describedAs("Should have 10 messages; even indices should have actual log messages")
                .hasSize(10)
                .contains(first(messages), atIndex(0))
                .contains(second(messages), atIndex(2))
                .contains(third(messages), atIndex(4))
                .contains(fourth(messages), atIndex(6))
                .contains(fifth(messages), atIndex(8));

        var timeSpentMessages = eventMessages.stream()
                .filter(eventMessage -> eventMessage.startsWith("[elapsed time since previous:"))
                .toList();

        assertThat(timeSpentMessages)
                .describedAs("Should have five messages with elapsed time")
                .hasSize(5);

        assertThat(first(timeSpentMessages))
                .describedAs("First elapsed time message should say there is no previous timestamp")
                .isEqualTo("[elapsed time since previous: N/A (no previous timestamp)]");

        subListExcludingFirst(timeSpentMessages).forEach(timeSpentMessage ->
                assertThat(timeSpentMessage)
                        .describedAs("Rest of elapsed time messages should include nanoseconds and millis")
                        .startsWith("[elapsed time since previous: ")
                        .contains(" nanoseconds / ")
                        .endsWith(" millis]"));
    }

    @Test
    void shouldLogAppendingElapsedTimeWithArguments() {
        timestampingLogger.logAppendingElapsed(Level.TRACE, "{} created with id {}", "User", 24);
        timestampingLogger.logAppendingElapsed(Level.TRACE, "{} updated with id {}", "Order", 84);

        List<String> eventMessages = appender.getOrderedEventMessages();

        assertThat(eventMessages).hasSize(2);
        assertThat(first(eventMessages))
                .isEqualTo("User created with id 24 [elapsed time since previous: N/A (no previous timestamp)]");
        assertThat(second(eventMessages))
                .startsWith("Order updated with id 84 [elapsed time since previous: ")
                .contains(" nanoseconds / ")
                .endsWith(" millis]");
    }

    @Test
    void shouldNotLogAppendingElapsedTimeWhenLevelIsInactive() {
        logbackLogger.setLevel(ch.qos.logback.classic.Level.ERROR);

        timestampingLogger.logAppendingElapsed(Level.WARN, "Should not see this!");
        assertThat(appender.getOrderedEventMessages()).isEmpty();
    }

    @Test
    void shouldTraceLogAppendingElapsedTime() {
        messages.forEach(message -> timestampingLogger.traceLogAppendingElapsed(message));
        assertAppendedElapsedEventMessages(Level.TRACE);
    }

    @Test
    void shouldDebugLogAppendingElapsedTime() {
        messages.forEach(message -> timestampingLogger.debugLogAppendingElapsed(message));
        assertAppendedElapsedEventMessages(Level.DEBUG);
    }

    @Test
    void shouldLogAppendingElapsedTime() {
        messages.forEach(message -> timestampingLogger.logAppendingElapsed(Level.WARN, message));
        assertAppendedElapsedEventMessages(Level.WARN);
    }

    private void assertAppendedElapsedEventMessages(Level expectedLevel) {
        List<ILoggingEvent> orderedEvents = appender.getOrderedEvents();
        var expectedLogbackLevel = ch.qos.logback.classic.Level.convertAnSLF4JLevel(expectedLevel);
        assertThat(orderedEvents)
                .describedAs("All events should have level %s", expectedLevel)
                .isNotEmpty()
                .allMatch(event -> event.getLevel() == expectedLogbackLevel);

        List<String> eventMessages = appender.getOrderedEventMessages();
        assertThat(eventMessages)
                .describedAs("Should have 5 messages")
                .hasSize(5);

        assertThat(first(eventMessages))
                .describedAs("First appended elapsed time message should say there is no previous timestamp")
                .isEqualTo("At time 0 [elapsed time since previous: N/A (no previous timestamp)]");

        IntStream.range(1, 4).forEach(i ->
                assertThat(eventMessages.get(i))
                        .describedAs("Rest of appended elapsed time messages should include nanoseconds and millis")
                        .startsWith("At time " + i + " [elapsed time since previous: ")
                        .contains(" nanoseconds / ")
                        .endsWith(" millis]"));
    }

    record LoggedMessages(List<String> logMessages, List<String> elapsedTimeMessages) {
        /**
         * Split all event messages into two lists: one containing the log messages, the other containing elapsed time
         * messages. The input list must have an even number of elements.
         */
        static LoggedMessages from(List<String> eventMessages) {
            checkEvenItemCount(eventMessages);
            var logMessages = new ArrayList<String>();
            var elapsedTimeMessages = new ArrayList<String>();
            for (int i = 0; i < eventMessages.size(); i++) {
                var message = eventMessages.get(i);
                if (i % 2 == 0) {
                    logMessages.add(message);
                } else {
                    elapsedTimeMessages.add(message);
                }
            }
            return new LoggedMessages(logMessages, elapsedTimeMessages);
        }
    }

    @Nested
    class Customization {

        @Nested
        class WhenLoggingElapsedTimeSeparately {

            @Test
            void shouldAllowCustomTemplate() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .build();

                logElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();
                var loggedMessages = LoggedMessages.from(eventMessages);

                assertThat(loggedMessages.logMessages).containsExactlyElementsOf(messages);

                var elapsedTimeMessages = loggedMessages.elapsedTimeMessages;
                assertThat(elapsedTimeMessages).hasSize(5);
                assertThat(first(elapsedTimeMessages)).isEqualTo("[elapsed time since previous: N/A (no previous timestamp)]");
                var restOfElapsedTimeMessages = KiwiLists.subListExcludingFirst(elapsedTimeMessages);
                assertThat(restOfElapsedTimeMessages)
                        .hasSize(4)
                        .allSatisfy(str -> assertThat(str).startsWith("[ELAPSED: ").endsWith("ms]"));
            }

            @Test
            void shouldAllowCustomInitialMessage() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .initialMessage("[MARK]")
                        .build();

                logElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();
                var loggedMessages = LoggedMessages.from(eventMessages);

                assertThat(loggedMessages.logMessages).containsExactlyElementsOf(messages);

                var elapsedTimeMessages = loggedMessages.elapsedTimeMessages;
                assertThat(elapsedTimeMessages).hasSize(5);
                assertThat(first(elapsedTimeMessages)).isEqualTo("[MARK]");
            }

            @Test
            void shouldAllowSkippingInitialMessage() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .skipInitialMessage(true)
                        .build();

                logElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();

                assertThat(eventMessages).hasSize(9);
                assertThat(first(eventMessages)).isEqualTo("At time 0");
                assertThat(second(eventMessages)).isEqualTo("At time 1");
                assertThat(third(eventMessages)).startsWith("[ELAPSED: ");
            }

            @Test
            void shouldAllowSettingAnInitialTimestamp() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .initialTimestamp(System.nanoTime())
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .build();

                logElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();
                var loggedMessages = LoggedMessages.from(eventMessages);

                assertThat(loggedMessages.logMessages).containsExactlyElementsOf(messages);

                var elapsedTimeMessages = loggedMessages.elapsedTimeMessages;
                assertThat(elapsedTimeMessages)
                        .hasSize(5)
                        .allSatisfy(str -> assertThat(str).startsWith("[ELAPSED: ").endsWith("ms]"));
            }

            @Test
            void shouldAllowArgumentTransformation() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .initialMessage("[MARK: T0]")
                        .elapsedTimeTemplate("[MARK: T{}: {} ({}ns, {}ms, {}s)]")
                        .argumentTransformer((diffInNanos, logCount) -> {
                            var nanoDuration = Duration.ofNanos(diffInNanos);
                            return new Object[] {
                                    logCount,
                                    KiwiDurationFormatters.formatDurationWords(nanoDuration),
                                    diffInNanos,
                                    nanoDuration.toMillis(),
                                    nanoDuration.toSeconds()};
                        })
                        .build();

                logElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();
                var loggedMessages = LoggedMessages.from(eventMessages);

                assertThat(loggedMessages.logMessages).containsExactlyElementsOf(messages);

                var elapsedTimeMessages = loggedMessages.elapsedTimeMessages;
                assertThat(elapsedTimeMessages).hasSize(5);
                assertThat(first(elapsedTimeMessages)).isEqualTo("[MARK: T0]");
                var restOfElapsedTimeMessages = KiwiLists.subListExcludingFirst(elapsedTimeMessages);
                assertThat(first(restOfElapsedTimeMessages)).startsWith("[MARK: T1");
                assertThat(last(restOfElapsedTimeMessages)).startsWith("[MARK: T4");
                assertThat(restOfElapsedTimeMessages)
                        .hasSize(4)
                        .allSatisfy(str -> assertThat(str).startsWith("[MARK: T").endsWith("s)]"));
            }

            private void logElapsedForAllMessagesUsing(TimestampingLogger customLogger) {
                messages.forEach(message -> customLogger.logElapsed(Level.WARN, message));
            }
        }

        @Nested
        class WhenLoggingElapsedTimeByAppending {

            @Test
            void shouldAllowCustomTemplate() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .build();

                logAppendingElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();

                assertThat(eventMessages).hasSize(5);
                assertThat(first(eventMessages)).isEqualTo("At time 0 [elapsed time since previous: N/A (no previous timestamp)]");
                List<String> restOfMessages = KiwiLists.subListExcludingFirst(eventMessages);
                assertThat(restOfMessages)
                        .hasSize(4)
                        .allSatisfy(str -> assertThat(str).startsWith("At time").contains(" [ELAPSED: ").endsWith("ms]"));
            }

            @Test
            void shouldAllowCustomInitialMessage() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .initialMessage("[MARK]")
                        .build();

                logAppendingElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();

                assertThat(eventMessages).hasSize(5);
                assertThat(first(eventMessages)).isEqualTo("At time 0 [MARK]");
            }

            @Test
            void shouldAllowSkippingInitialMessage() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .skipInitialMessage(true)
                        .build();

                logAppendingElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();

                assertThat(eventMessages).hasSize(5);
                assertThat(first(eventMessages)).isEqualTo("At time 0");
            }

            @Test
            void shouldAllowSettingAnInitialTimestamp() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .initialTimestamp(System.nanoTime())
                        .elapsedTimeTemplate("[ELAPSED: {}ns, {}ms]")
                        .build();

                logAppendingElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();

                assertThat(eventMessages)
                        .hasSize(5)
                        .allSatisfy(str -> assertThat(str).startsWith("At time ").contains("[ELAPSED: ").endsWith("ms]"));
            }

            @Test
            void shouldAllowArgumentTransformation() {
                var customLogger = TimestampingLogger.builder()
                        .logger(logbackLogger)
                        .initialMessage("[MARK: T0]")
                        .elapsedTimeTemplate("[MARK: T{}: {} ({}ns, {}ms, {}s)]")
                        .argumentTransformer((diffInNanos, logCount) -> {
                            var nanoDuration = Duration.ofNanos(diffInNanos);
                            return new Object[] {
                                    logCount,
                                    KiwiDurationFormatters.formatDurationWords(nanoDuration),
                                    diffInNanos,
                                    nanoDuration.toMillis(),
                                    nanoDuration.toSeconds()};
                        })
                        .build();

                logAppendingElapsedForAllMessagesUsing(customLogger);

                List<String> eventMessages = appender.getOrderedEventMessages();

                assertThat(eventMessages).hasSize(5);
                assertThat(first(eventMessages)).isEqualTo("At time 0 [MARK: T0]");
                var restOfMessages = KiwiLists.subListExcludingFirst(eventMessages);
                assertThat(first(restOfMessages)).startsWith("At time 1 [MARK: T1");
                assertThat(last(restOfMessages)).startsWith("At time 4 [MARK: T4");
                assertThat(restOfMessages)
                        .hasSize(4)
                        .allSatisfy(str -> assertThat(str).startsWith("At time ").contains("[MARK: T").endsWith("s)]"));
            }

            private void logAppendingElapsedForAllMessagesUsing(TimestampingLogger customLogger) {
                messages.forEach(message -> customLogger.logAppendingElapsed(Level.WARN, message));
            }
        }
    }
}
