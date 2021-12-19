package org.kiwiproject.beta.base.misc;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.TWO_HUNDRED_MILLISECONDS;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.DefaultEnvironment;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Trying out an idea for a self-contained thread-safe counter that drains itself on a recurring basis. The
 * class under test is currently defined in this test class; it isn't in src/main right now.
 * <p>
 * Client code determines the "drain period" after which the count is reset to zero, and can increment as often
 * as it needs to. Clients can obtain the current count at any time. Clients should ensure the counter is "started"
 * before using it, otherwise it won't drain. No checks are done on this in this first implementation.
 */
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

    // TODO: draining tests...

    // TODO:
    //  implement DW Managed?
    //  implement Closeable?
    //  better way than to use synchronized on start() or is that the "best" way?

    @SuppressWarnings("ALL")
    static class AutoDrainingCounter {

        private final AtomicInteger count;
        private final Duration drainPeriod;
        private final Consumer<Integer> drainCallback;
        private final ScheduledExecutorService scheduledExecutor;
        private final AtomicBoolean counting;

        public AutoDrainingCounter(Duration drainPeriod) {
            this(drainPeriod, null);
        }

        /**
         * If a drain callback is provided, its implementation should return quickly, since the current
         * implementation calls it synchronously. Callback implementations can execute asynchronously if desired.
         */
        public AutoDrainingCounter(Duration drainPeriod, @Nullable Consumer<Integer> drainCallback) {
            this.count = new AtomicInteger();
            this.drainPeriod = drainPeriod;
            this.drainCallback = drainCallback;
            this.scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
            this.counting = new AtomicBoolean();
        }

        public static AutoDrainingCounter createAndStart(Duration drainPeriod) {
            return createAndStart(drainPeriod, null);
        }

        public static AutoDrainingCounter createAndStart(Duration drainPeriod,
                                                         @Nullable Consumer<Integer> drainCallback) {
            var counter = new AutoDrainingCounter(drainPeriod, drainCallback);
            counter.start();
            return counter;
        }

        public synchronized void start() {
            if (counting.get()) {
                throw new IllegalStateException("counter already started");
            }

            var periodMillis = drainPeriod.toMillis();
            scheduledExecutor.scheduleWithFixedDelay(
                    () -> drain(), periodMillis, periodMillis, TimeUnit.MILLISECONDS);

            counting.set(true);
        }

        private void drain() {
            var oldCount = count.getAndSet(0);
            if (nonNull(drainCallback)) {
                drainCallback.accept(oldCount);
            }
            LOG.trace("Drained counter. Old count was: {}", oldCount);
        }

        public boolean isAlive() {
            return counting.get();
        }

        public void stop() {
            counting.set(false);
            scheduledExecutor.shutdownNow();
        }

        public int get() {
            return count.get();
        }

        public int getAndIncrement() {
            return count.getAndIncrement();
        }

        public int incrementAndGet() {
            return count.incrementAndGet();
        }
    }
}
