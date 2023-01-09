package org.kiwiproject.beta.util.comparator;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@UtilityClass
public class KiwiComparators {

    public static Comparator<Instant> comparingInstantTruncatedToMillis() {
        return Comparator.comparing(i -> i.truncatedTo(ChronoUnit.MILLIS));
    }

    public static Comparator<LocalDateTime> comparingLocalDateTimeTruncatedToMillis() {
        return Comparator.comparing(ldt -> ldt.truncatedTo(ChronoUnit.MILLIS));
    }

    public static Comparator<ZonedDateTime> comparingZonedDateTimeTruncatedToMillis() {
        return Comparator.comparing(zdt -> zdt.truncatedTo(ChronoUnit.MILLIS));
    }

    public static Comparator<OffsetDateTime> comparingOffsetDateTimeTruncatedToMillis() {
        return Comparator.comparing(odt -> odt.truncatedTo(ChronoUnit.MILLIS));
    }
}
