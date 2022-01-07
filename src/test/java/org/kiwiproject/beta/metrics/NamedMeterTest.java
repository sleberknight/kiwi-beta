package org.kiwiproject.beta.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.codahale.metrics.Meter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@DisplayName("NamedMeter")
class NamedMeterTest {

    @Test
    void shouldCreateFromMeter() {
        var meter = new Meter();
        var namedMeter = NamedMeter.of("testMeter", meter);

        assertThat(namedMeter.getName()).isEqualTo("testMeter");
        assertThat(namedMeter.getMeter()).isSameAs(meter);
    }

    @ParameterizedTest(name = "[{index}] name=\"{0}\", meter={1}")
    @MethodSource("invalidFactoryMethodArgs")
    void shouldNotCreateFromInvalidArguments(String name, Meter meter, String expectedErrorMessage) {
        assertThatIllegalArgumentException().isThrownBy(() -> NamedMeter.of(name, meter))
                .withMessage(expectedErrorMessage);
    }

    static Stream<Arguments> invalidFactoryMethodArgs() {
        // overriding toString in this Meter so that the test display name renders nicely
        var meter = new Meter() {
            @Override
            public String toString() {
                return "[a non-null meter]";
            }
        };

        var nameError = "name must not be blank";
        var meterError = "meterDelegate must not be null";

        return Stream.<Arguments>builder()
                .add(arguments(null, meter, nameError))
                .add(arguments("", meter, nameError))
                .add(arguments(" ", meter, nameError))
                .add(arguments("testMeter", null, meterError))
                .build();
    }

    @Test
    void shouldReturnNameAsToString() {
        var name = "theMeter";
        var namedMeter = NamedMeter.of(name, new Meter());
        assertThat(namedMeter).hasToString(name);
    }
}