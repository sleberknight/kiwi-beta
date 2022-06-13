package org.kiwiproject.beta.test.logback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.kiwiproject.collect.KiwiLists.first;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

@DisplayName("InMemoryAppender")
class InMemoryAppenderTest {

    private static final String TEST_CLASS_NAME = InMemoryAppenderTest.class.getName();

    private Logger logger;
    private InMemoryAppender appender;

    @BeforeEach
    void setUp() {
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(TEST_CLASS_NAME);
        appender = (InMemoryAppender) logger.getAppender("MEMORY");
    }

    @AfterEach
    void tearDown() {
        appender.clearEvents();
    }

    @Test
    void shouldAppendAnEvent() {
        logger.info("Hello, logger");

        assertThat(appender.getOrderedEvents()).hasSize(1);
        var event = first(appender.getOrderedEvents());
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).isEqualTo("Hello, logger");
    }

    @Nested
    class GetEventMap {

        @BeforeEach
        void setUp() {
            logger.info("Message A");
            logger.debug("Message B");
            logger.warn("Message C");
            logger.error("Message D");
        }

        @Test
        void shouldReturnEvents() {
            var eventMap = appender.getEventMap();

            assertThat(eventMap.get(1).getFormattedMessage()).isEqualTo("Message A");
            assertThat(eventMap.get(2).getFormattedMessage()).isEqualTo("Message B");
            assertThat(eventMap.get(3).getFormattedMessage()).isEqualTo("Message C");
            assertThat(eventMap.get(4).getFormattedMessage()).isEqualTo("Message D");
        }

        @Test
        void shouldReturnUnmodifiableCopyOfEventMap() {
            var eventMap = appender.getEventMap();

            var newEvent = new LoggingEvent();
            assertThatThrownBy(() -> eventMap.put(2, newEvent)).isExactlyInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void shouldReturnCopyOfEventMap() {
            var originalEventMap = appender.getEventMap();

            logger.info("Another message");
            logger.debug("And another");

            var newEventMap = appender.getEventMap();
            assertThat(newEventMap).hasSize(originalEventMap.size() + 2);
        }
    }


    @Test
    void shouldReturnEventsInOrderTheyWereLogged() {
        logger.info("Message 1");
        logger.debug("Message 2");
        logger.warn("Message 3");

        assertThat(appender.getOrderedEvents())
                .extracting("level", "formattedMessage")
                .containsExactly(
                        tuple(Level.INFO, "Message 1"),
                        tuple(Level.DEBUG, "Message 2"),
                        tuple(Level.WARN, "Message 3")
                );
    }

    @Test
    void shouldReturnOrderedLogMessages() {
        logger.info("Message 1");
        logger.debug("Message 2");
        logger.warn("Message 3");
        logger.error("Message 4");

        assertThat(appender.getOrderedEventMessages()).containsExactly(
                "Message 1",
                "Message 2",
                "Message 3",
                "Message 4"
        );
    }

    @Test
    void shouldClearAllLoggingEvents() {
        IntStream.range(0, 10).forEach(i -> logger.debug("Message {}", i));
        assertThat(appender.getOrderedEvents()).hasSize(10);

        appender.clearEvents();

        assertThat(appender.getOrderedEvents()).isEmpty();
    }

    @Nested
    class AssertNumberOfLoggingEventsAndGet {

        @Test
        void shouldAssertWhenEmpty() {
            var events = appender.assertNumberOfLoggingEventsAndGet(0);
            assertThat(events).isEmpty();
        }

        @Test
        void shouldAssertWhenContainsLoggingEvents() {
            var messages = IntStream.range(0, 5).mapToObj(i -> "Message " + i).toArray(String[]::new);
            Arrays.stream(messages).forEach(logger::debug);

            var eventsRef = new AtomicReference<List<ILoggingEvent>>();
            assertThatCode(() -> {
                var loggingEvents = appender.assertNumberOfLoggingEventsAndGet(5);
                eventsRef.set(loggingEvents);
            }).doesNotThrowAnyException();

            var events = eventsRef.get();
            assertThat(events).extracting(ILoggingEvent::getFormattedMessage).containsExactly(messages);
        }
    }
}
