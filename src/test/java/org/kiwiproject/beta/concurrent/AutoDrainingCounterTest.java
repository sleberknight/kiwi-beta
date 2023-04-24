package org.kiwiproject.beta.concurrent;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.DefaultEnvironment;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@DisplayName("AutoDrainingCounter")
@Slf4j
class AutoDrainingCounterTest {

    private static final Duration DRAIN_PERIOD = Duration.ofSeconds(5);
    private static final Duration TWENTY_FIVE_MILLISECONDS = Duration.ofMillis(25);

    private AutoDrainingCounter counter;

    @AfterEach
    void tearDown() {
        if (nonNull(counter) && counter.isAlive()) {
            counter.stop();
        }
    }

    /**
     * Not a real test, which is why it is disabled. Plus, it runs for almost a minute. This is just a bit of hacking
     * and a simple proof-of-concept. You can run the "test" individually in IntelliJ, but it will be skipped when run
     * via Maven or when the test class is run (even in IntelliJ).
     */
    @SuppressWarnings({"java:S2699", "java:S1607"})
    @Test
    @Disabled
    void runExample() {
        var drainPeriod = Duration.ofSeconds(10);
        var counter = AutoDrainingCounter.createAndStart(drainPeriod);

        runCounter(counter);
    }

    /**
     * Same as above "test".
     */
    @SuppressWarnings({"java:S2699", "java:S1607"})
    @Test
    @Disabled
    void runExampleWithCallback() {
        var drainPeriod = Duration.ofSeconds(10);
        var totalCount = new AtomicInteger();
        var counter = AutoDrainingCounter.createAndStart(drainPeriod, totalCount::addAndGet);

        runCounter(counter);

        LOG.info("Total count: {}", totalCount.get());
    }

    private void runCounter(AutoDrainingCounter counter) {
        var scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        int delay = 750;  // millis
        scheduledExecutor.scheduleAtFixedRate(() -> {
            if (ThreadLocalRandom.current().nextInt(10) > 7) {
                counter.incrementAndGet();
            }
            var currentCount = counter.get();
            LOG.info("Current count: {}", currentCount);

        }, delay, delay, TimeUnit.MILLISECONDS);

        new DefaultEnvironment().sleepQuietly(55, TimeUnit.SECONDS);

        counter.stop();
    }

    @Test
    void shouldNotStartWhenCreatedUsingConstructor() {
        counter = new AutoDrainingCounter(DRAIN_PERIOD);
        assertThat(counter.isAlive()).isFalse();
    }

    @Test
    void shouldStartWhenCreatedUsingFactoryMethod() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);
        assertThat(counter.isAlive()).isTrue();
    }

    @Test
    void shouldNotAllowStartOnceAlive() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);

        assertThatIllegalStateException().isThrownBy(counter::start).withMessage("counter already started");
    }

    @Test
    void shouldStart() {
        counter = new AutoDrainingCounter(DRAIN_PERIOD);
        counter.start();
        assertThat(counter.isAlive()).isTrue();
    }

    @Test
    void shouldStop() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);
        counter.stop();
        assertThat(counter.isAlive()).isFalse();
    }

    @Test
    void shouldIgnoreStop_WhenNeverStarted() {
        counter = new AutoDrainingCounter(DRAIN_PERIOD);
        counter.stop();
        assertThat(counter.isAlive()).isFalse();
    }

    @Test
    void shouldIgnoreStop_WhenAlreadyStopped() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);
        counter.stop();
        assertThat(counter.isAlive()).isFalse();
        counter.stop();
        assertThat(counter.isAlive()).isFalse();
    }

    @Test
    void shouldClose() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);
        counter.close();
        assertThat(counter.isAlive()).isFalse();
    }

    @Test
    void shouldGet() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);
        assertThat(counter.get()).isZero();

        counter.incrementAndGet();
        counter.incrementAndGet();
        counter.incrementAndGet();

        assertThat(counter.get()).isEqualTo(3);
    }

    @Test
    void shouldGetAndIncrement() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);

        assertThat(counter.getAndIncrement()).isZero();

        assertThat(counter.getAndIncrement()).isOne();

        assertThat(counter.getAndIncrement()).isEqualTo(2);

        assertThat(counter.getAndIncrement()).isEqualTo(3);
    }

    @Test
    void shouldIncrementAndGet() {
        counter = AutoDrainingCounter.createAndStart(DRAIN_PERIOD);

        assertThat(counter.incrementAndGet()).isOne();

        assertThat(counter.incrementAndGet()).isEqualTo(2);

        assertThat(counter.incrementAndGet()).isEqualTo(3);

        assertThat(counter.incrementAndGet()).isEqualTo(4);
    }

    @Test
    void shouldExecuteCallbackWhenProvided() {
        var drainPeriod = Duration.ofMillis(50);
        var totalCount = new AtomicInteger();
        counter = AutoDrainingCounter.createAndStart(drainPeriod, totalCount::addAndGet);

        counter.incrementAndGet();
        counter.incrementAndGet();
        counter.incrementAndGet();
        waitUntilTotalCountEquals(totalCount, 3);

        counter.incrementAndGet();
        counter.incrementAndGet();
        waitUntilTotalCountEquals(totalCount, 5);
    }

    private void waitUntilTotalCountEquals(AtomicInteger totalCount, int expectedValue) {
        await().pollInterval(TWENTY_FIVE_MILLISECONDS)
                .atMost(TWO_HUNDRED_MILLISECONDS)
                .until(() -> totalCount.get() == expectedValue);
    }

    @Test
    void shouldDrain() {
        var drainPeriod = Duration.ofMillis(30);
        counter = new AutoDrainingCounter(drainPeriod);

        incrementCounter(5);

        counter.start();

        waitUntilDrained();

        incrementCounter(2);

        waitUntilDrained();

        incrementCounter(4);

        waitUntilDrained();
    }

    @Test
    void shouldDrain_AndTriggerCallback() {
        var drainPeriod = Duration.ofMillis(30);
        var drainCount = new AtomicInteger();

        counter = new AutoDrainingCounter(drainPeriod, drainCount::addAndGet);
        incrementCounter(3);

        counter.start();

        waitUntilDrained();
        assertThat(drainCount).hasValue(3);

        incrementCounter(2);
        waitUntilDrained();
        assertThat(drainCount).hasValue(5);

        incrementCounter(4);
        waitUntilDrained();
        assertThat(drainCount).hasValue(9);
    }

    private void incrementCounter(int times) {
        IntStream.range(0, times).forEach(i -> counter.incrementAndGet());
    }

    private void waitUntilDrained() {
        await().pollInterval(TWENTY_FIVE_MILLISECONDS)
                .atMost(ONE_HUNDRED_MILLISECONDS)
                .until(() -> counter.get() == 0);
    }
}
