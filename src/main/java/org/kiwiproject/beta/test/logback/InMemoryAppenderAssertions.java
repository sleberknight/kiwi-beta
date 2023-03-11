package org.kiwiproject.beta.test.logback;

import static java.util.stream.Collectors.toList;

import com.google.common.annotations.Beta;
import org.assertj.core.api.Assertions;

import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * AssertJ assertions for {@link InMemoryAppender}.
 */
@Beta
public class InMemoryAppenderAssertions {

    private final InMemoryAppender appender;

    private InMemoryAppenderAssertions(InMemoryAppender appender) {
        this.appender = appender;
    }

    /**
     * Begin assertions for an {@link InMemoryAppender}.
     *
     * @param appender the appender to assert against
     * @return a new InMemoryAppenderAssertions instance
     */
    public static InMemoryAppenderAssertions assertThat(InMemoryAppender appender) {
        return new InMemoryAppenderAssertions(appender);
    }

    /**
     * Assert this appender has the expected number of logging events, and if the assertion succeeds, return a
     * list containing those events.
     *
     * @return a List containining the logging events
     */
    public List<ILoggingEvent> hasNumberOfLoggingEventsAndGet(int expectedEventCount) {
        hasNumberOfLoggingEvents(expectedEventCount);
        return andGetOrderedEvents();
    }

    /**
     * Assert this appender has the expected number of logging events, and if the assertion succeeds, return this
     * instance to continue chaining assertions.
     *
     * @return this instance
     */
    public InMemoryAppenderAssertions hasNumberOfLoggingEvents(int expectedEventCount) {
        var events = appender.getOrderedEvents();
        Assertions.assertThat(events).hasSize(expectedEventCount);
        return this;
    }

    /**
     * Assert this appender contains the given message at least once (but possibly more than once).
     *
     * @param message the exact message to find
     * @return this instance
     */
    public InMemoryAppenderAssertions containsMessage(String message) {
        var messages = appender.getOrderedEvents().stream().map(ILoggingEvent::getMessage).collect(toList());
        Assertions.assertThat(messages).contains(message);
        return this;
    }

    /**
     * A terminal method if you want to get the actual logging events after performing assertions, for example
     * to perform additional inspections. Does not perform any assertions.
     *
     * @return a List containining the logging events
     */
    public List<ILoggingEvent> andGetOrderedEvents() {
        return appender.getOrderedEvents();
    }
}
