package org.kiwiproject.beta.test.logback;

import static java.util.Comparator.comparing;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.common.annotations.Beta;
import org.kiwiproject.base.KiwiDeprecated;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * A logback appender that stores logging events in an in-memory map.
 * <p>
 * This is for testing purposes only, and is not at all intended for production use!
 *
 * @deprecated replaced by InMemoryAppender in
 * <a href="https://github.com/kiwiproject/kiwi-test/">kiwi-test</a> 3.2.0
 */
@Beta
@Deprecated(since = "1.3.0", forRemoval = true)
@KiwiDeprecated(replacedBy = "InMemoryAppender in kiwi-test 3.2.0")
@SuppressWarnings("java:S1133")
public class InMemoryAppender extends AppenderBase<ILoggingEvent> {

    private final AtomicInteger messageOrder;
    private final ConcurrentMap<Integer, ILoggingEvent> eventMap;

    public InMemoryAppender() {
        this.messageOrder = new AtomicInteger();
        this.eventMap = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void append(ILoggingEvent eventObject) {
        eventMap.put(messageOrder.incrementAndGet(), eventObject);
    }

    /**
     * Clear all the events that are stored in-memory.
     */
    public synchronized void clearEvents() {
        messageOrder.set(0);
        eventMap.clear();
    }

    /**
     * Return a copy of the internal event map. The keys are the message order starting at one, and the values
     * are the corresponding logging events.
     *
     * @return an unmodifiable copy of the event map
     */
    public Map<Integer, ILoggingEvent> getEventMap() {
        return Map.copyOf(eventMap);
    }

    /**
     * Return the logged {@link ILoggingEvent} instances.
     *
     * @return a list containing the logged events
     */
    public List<ILoggingEvent> getOrderedEvents() {
        return getOrderedEventStream().toList();
    }

    /**
     * Return the logged messages.
     *
     * @return a list containing the logged event messages
     */
    public List<String> getOrderedEventMessages() {
        return getOrderedEventStream()
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    /**
     * Return a stream containing the logged events.
     *
     * @return a stream of the logged events
     */
    public Stream<ILoggingEvent> getOrderedEventStream() {
        return eventMap.values()
                .stream()
                .sorted(comparing(ILoggingEvent::getTimeStamp));
    }
}
