package org.kiwiproject.beta.health;

import static org.kiwiproject.metrics.health.HealthCheckResults.SEVERITY_DETAIL;
import static org.kiwiproject.test.assertj.dropwizard.metrics.HealthCheckResultAssertions.assertThatHealthCheck;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Meter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.metrics.health.HealthStatus;

import java.util.concurrent.TimeUnit;

@DisplayName("SimpleMeterHealthCheck")
class SimpleMeterHealthCheckTest {

    private static final long NUM_SECONDS_IN_15_MINUTES = TimeUnit.MINUTES.toSeconds(15);
    private static final double RATE_FOR_ONE_ERROR_IN_15_MINUTES = 1.0 / NUM_SECONDS_IN_15_MINUTES;

    private SimpleMeterHealthCheck healthCheck;
    private Meter meter;

    @BeforeEach
    void setUp() {
        meter = mock(Meter.class);
        healthCheck = new SimpleMeterHealthCheck("testMeterHealthCheck", meter);
    }

    @Test
    void shouldBeHealthy_WhenNoErrors() {
        when(meter.getFifteenMinuteRate()).thenReturn(0.0);

        assertThatHealthCheck(healthCheck)
                .isHealthy()
                .hasMessage("No errors in the last 15 minutes")
                .hasDetail(SEVERITY_DETAIL, HealthStatus.OK.name());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.8, 0.9, 0.99, 0.999, 0.9999})
    void shouldBeHealthy_WhenLessThanOneError(double fractionOfOneErrorRate) {
        when(meter.getFifteenMinuteRate()).thenReturn(fractionOfOneErrorRate * RATE_FOR_ONE_ERROR_IN_15_MINUTES);

        assertThatHealthCheck(healthCheck)
                .isHealthy()
                .hasMessage("No errors in the last 15 minutes")
                .hasDetail(SEVERITY_DETAIL, HealthStatus.OK.name());
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.0, 1.1, 10.0, 25.0, 50.0})
    void shouldBeUnhealthy_WhenSomeErrors(double multipleOfOneErrorRate) {
        when(meter.getFifteenMinuteRate()).thenReturn(multipleOfOneErrorRate * RATE_FOR_ONE_ERROR_IN_15_MINUTES);

        assertThatHealthCheck(healthCheck)
                .isUnhealthy()
                .hasMessage("Some errors in the last 15 minutes")
                .hasDetail(SEVERITY_DETAIL, HealthStatus.WARN.name());
    }
}
