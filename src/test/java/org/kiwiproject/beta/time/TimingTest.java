package org.kiwiproject.beta.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

@DisplayName("Timing")
class TimingTest {

    private static final KiwiEnvironment ENV = new DefaultEnvironment();

    private StopWatch stopWatch;

    @BeforeEach
    void setUp() {
        stopWatch = new StopWatch();
    }

    @RepeatedTest(10)
    void shouldTimeOperationsThatReturnResult() {
        Supplier<Integer> op = () -> {
            sleepSmallRandomTime();
            return 42 * 42;
        };
        var result = Timing.timeWithResult(stopWatch, op);

        assertThat(result.getElapsedMillis()).isPositive();
        assertThat(result.getResult()).isEqualTo(1764);
        assertThat(stopWatch.isStopped()).isTrue();
        assertThat(stopWatch.getTime()).isEqualTo(result.getElapsedMillis());
    }

    @RepeatedTest(10)
    void shouldStopStopWatch_WhenExceptionsAreThrown_FromTimedOperationWithResult() {
        Supplier<Integer> op = () -> {
            sleepSmallRandomTime();
            throw new RuntimeException("oops");
        };

        assertThatThrownBy(() -> Timing.timeWithResult(stopWatch, op))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("oops");
        assertThat(stopWatch.isStopped()).isTrue();
        assertThat(stopWatch.getTime()).isPositive();
    }

    @RepeatedTest(10)
    void shouldTimeOperationsThatDoNotReturnResult() {
        Runnable op = TimingTest::sleepSmallRandomTime;
        var result = Timing.timeNoResult(stopWatch, op);

        assertThat(result.getElapsedMillis()).isPositive();
        assertThat(stopWatch.isStopped()).isTrue();
        assertThat(stopWatch.getTime()).isEqualTo(result.getElapsedMillis());
    }

    @RepeatedTest(10)
    void shouldStopStopWatch_WhenExceptionsAreThrown_FromTimedOperationWithNoResult() {
        Runnable op = () -> {
            sleepSmallRandomTime();
            throw new RuntimeException("ugh");
        };

        assertThatThrownBy(() -> Timing.timeNoResult(stopWatch, op))
                .isExactlyInstanceOf(RuntimeException.class)
                .hasMessage("ugh");
        assertThat(stopWatch.isStopped()).isTrue();
        assertThat(stopWatch.getTime()).isPositive();
    }

    private static void sleepSmallRandomTime() {
        ENV.sleepQuietly(ThreadLocalRandom.current().nextLong(10, 20));
    }
}
