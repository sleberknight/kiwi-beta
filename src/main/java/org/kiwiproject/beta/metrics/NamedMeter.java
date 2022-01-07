package org.kiwiproject.beta.metrics;

import com.codahale.metrics.Meter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * A Metrics {@link Meter} that knows its own name.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NamedMeter extends Meter {

    @Getter
    private final String name;

    @Delegate
    private final Meter meter;

    public static NamedMeter of(String name, Meter meter) {
        return new NamedMeter(name, meter);
    }
}
