package org.kiwiproject.beta.health;

import static org.kiwiproject.metrics.health.HealthCheckResults.newHealthyResult;
import static org.kiwiproject.metrics.health.HealthCheckResults.newUnhealthyResult;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MovingAverages;
import com.codahale.metrics.health.HealthCheck;
import com.google.common.annotations.Beta;
import lombok.extern.slf4j.Slf4j;
import org.kiwiproject.beta.metrics.NamedMeter;
import org.kiwiproject.metrics.health.HealthCheckResults;

import java.util.concurrent.TimeUnit;

/**
 * Very simple health check that checks if a {@link Meter} has any errors in the last 15 minutes, calculated
 * using the 15-minute rate from the meter. The returned results are built using {@link HealthCheckResults} so
 * they contain a "severity" detail.
 *
 * @implNote If the {@link Meter} from Metrics library uses exponentially-weighted moving averages, it is
 * actually not trivial to compute an exact number of errors in the last N time units. Here, this is using
 * the 15-minute rate from the supplied meter and using that to estimate the number of errors. See specifically
 * {@link com.codahale.metrics.MovingAverages} and {@link Meter#Meter(MovingAverages)}.
 * @see HealthCheckResults
 */
@Slf4j
@Beta
public class SimpleMeterHealthCheck extends HealthCheck {

    private static final String HEALTHY_MESSAGE = "No errors in the last 15 minutes";
    private static final String UNHEALTHY_MESSAGE = "Some errors in the last 15 minutes";
    private static final long NUM_SECONDS_IN_15_MINUTES = TimeUnit.MINUTES.toSeconds(15);

    private final NamedMeter meter;

    public SimpleMeterHealthCheck(String name, Meter meter) {
        this.meter = NamedMeter.of(name, meter);
    }

    @Override
    protected Result check() {
        if (hasAnyErrorsInLast15Minutes(meter)) {
            return newUnhealthyResult(UNHEALTHY_MESSAGE);
        }

        return newHealthyResult(HEALTHY_MESSAGE);
    }

    private static boolean hasAnyErrorsInLast15Minutes(NamedMeter meter) {
        var fifteenMinuteRate = meter.getFifteenMinuteRate();
        double estimatedErrorCount = fifteenMinuteRate * NUM_SECONDS_IN_15_MINUTES;

        LOG.trace("Meter {}: 15 minute rate : {} , estimated error count: {}",
                meter.getName(),
                fifteenMinuteRate,
                estimatedErrorCount);

        return estimatedErrorCount >= 1.0;
    }
}
