package org.kiwiproject.beta.util.comparator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@DisplayName("KiwiComparators")
public class KiwiComparatorsTest {

    @Nested
    class ComparingInstantTruncatedToMillis {

        @Test
        void shouldEqualWhenMillisEqual() {
            var i1 = Instant.now();
            var i2 = i1.truncatedTo(ChronoUnit.MILLIS);
            assertThat(i1).isNotEqualTo(i2);

            assertThat(i1).usingComparator(KiwiComparators.comparingInstantTruncatedToMillis()).isEqualTo(i2);
        }

        @Test
        void shouldNotEqualWhenMillisAreNotEqual() {
            var i1 = Instant.now();
            var i2 = i1.plusMillis(42);
            assertThat(i1).isNotEqualTo(i2);

            assertThat(i1).usingComparator(KiwiComparators.comparingInstantTruncatedToMillis()).isNotEqualTo(i2);
        }
    }

    @Nested
    class ComparingLocalDateTimeTruncatedToMillis {

        @Test
        void shouldEqualWhenMillisEqual() {
            var ldt1 = LocalDateTime.now();
            var ldt2 = ldt1.truncatedTo(ChronoUnit.MILLIS);
            assertThat(ldt1).isNotEqualTo(ldt2);

            assertThat(ldt1).usingComparator(KiwiComparators.comparingLocalDateTimeTruncatedToMillis()).isEqualTo(ldt2);
        }

        @Test
        void shouldNotEqualWhenMillisAreNotEqual() {
            var ldt1 = LocalDateTime.now();
            var ldt2 = ldt1.plus(42, ChronoUnit.MILLIS);
            assertThat(ldt1).isNotEqualTo(ldt2);

            assertThat(ldt1).usingComparator(KiwiComparators.comparingLocalDateTimeTruncatedToMillis()).isNotEqualTo(ldt2);
        }
    }

    @Nested
    class ComparingZonedDateTimeTruncatedToMillis {

        @Test
        void shouldEqualWhenMillisEqual() {
            var zdt1 = ZonedDateTime.now();
            var zdt2 = zdt1.truncatedTo(ChronoUnit.MILLIS);
            assertThat(zdt1).isNotEqualTo(zdt2);

            assertThat(zdt1).usingComparator(KiwiComparators.comparingZonedDateTimeTruncatedToMillis()).isEqualTo(zdt2);
        }

        @Test
        void shouldNotEqualWhenMillisAreNotEqual() {
            var zdt1 = ZonedDateTime.now();
            var zdt2 = zdt1.plus(42, ChronoUnit.MILLIS);
            assertThat(zdt1).isNotEqualTo(zdt2);

            assertThat(zdt1).usingComparator(KiwiComparators.comparingZonedDateTimeTruncatedToMillis()).isNotEqualTo(zdt2);
        }
    }

    @Nested
    class ComparingOffsetDateTimeTruncatedToMillis {

        @Test
        void shouldEqualWhenMillisEqual() {
            var odt1 = OffsetDateTime.now();
            var odt2 = odt1.truncatedTo(ChronoUnit.MILLIS);
            assertThat(odt1).isNotEqualTo(odt2);

            assertThat(odt1).usingComparator(KiwiComparators.comparingOffsetDateTimeTruncatedToMillis()).isEqualTo(odt2);
        }

        @Test
        void shouldNotEqualWhenMillisAreNotEqual() {
            var odt1 = OffsetDateTime.now();
            var odt2 = odt1.plus(42, ChronoUnit.MILLIS);
            assertThat(odt1).isNotEqualTo(odt2);

            assertThat(odt1).usingComparator(KiwiComparators.comparingOffsetDateTimeTruncatedToMillis()).isNotEqualTo(odt2);
        }
    }
}
