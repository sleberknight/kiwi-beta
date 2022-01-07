package org.kiwiproject.beta.metrics;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * A Metrics {@link Meter} that knows its own name.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NamedMeter extends Meter {

    /**
     * Get the name of the {@link Meter}.
     */
    @Getter
    private final String name;

    /**
     * Allows access to the decorated {@link Meter} instance. Normally this won't be necessary since calls to
     * {@link Metered} methods are delegated to and because this class extends {@link Meter}, which allows it
     * to be passed directly to any methods that accept a {@link Meter}, e.g. a {@link com.codahale.metrics.Timer}
     * requires a {@link Meter} in one of its constructors.
     */
    @Getter
    @Delegate
    private final Meter meter;

    /**
     * Creates a new {@link NamedMeter} which delegates to the provided {@link Meter}.
     *
     * @param name          the name for this meter
     * @param meterDelegate the {@link Meter} delegate that method calls will be forwarded to
     * @return a new instance
     */
    public static NamedMeter of(String name, Meter meterDelegate) {
        checkArgumentNotBlank(name, "name must not be blank");
        checkArgumentNotNull(meterDelegate, "meterDelegate must not be null");

        return new NamedMeter(name, meterDelegate);
    }

    /**
     * @return the name of this Meter
     * @implNote This does not include any additional information from the Meter, because calling the methods
     * in a Meter cause changes to occur internally (since it is time-dependent) and a {@code toString()} should
     * not cause any internal state changes in an object.
     */
    @Override
    public String toString() {
        return name;
    }

}
