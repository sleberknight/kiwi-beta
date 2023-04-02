package org.kiwiproject.beta.concurrent;

import static java.util.Objects.nonNull;

import com.google.common.annotations.Beta;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Closeable;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Trying out an idea for a self-contained thread-safe counter that drains itself on a recurring basis.
 * <p>
 * Client code determines the "drain period" after which the count is reset to zero, and can increment as often
 * as it needs to. Clients can obtain the current count at any time. Clients should ensure the counter is "started"
 * before using it, otherwise it won't drain. No checks are done on this in this first implementation.
 * <p>
 * Clients can also supply a callback which will be called whenever the counter drains. The counter will pass
 * the count before draining to the callback. If a drain callback is provided, its implementation should return
 * quickly, since the current implementation calls it synchronously. Callback implementations can execute
 * asynchronously if desired.
 */
@Slf4j
@Beta
public class AutoDrainingCounter implements Closeable {

    private final AtomicInteger count;
    private final Duration drainPeriod;
    private final Consumer<Integer> drainCallback;
    private final ScheduledExecutorService scheduledExecutor;
    private final AtomicBoolean counting;

    public AutoDrainingCounter(Duration drainPeriod) {
        this(drainPeriod, null);
    }

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
                this::drain, periodMillis, periodMillis, TimeUnit.MILLISECONDS);

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

    /**
     * Simply calls {@link #stop()}. Implementing {@link Closeable} lets this class participate
     * in automatic resource management via th try-with-resources mechanism.
     */
    @Override
    public void close() {
        stop();
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
