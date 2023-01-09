package org.kiwiproject.beta.util.comparator;

import lombok.experimental.UtilityClass;

import com.google.common.annotations.Beta;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;

@UtilityClass
@Beta
public class KiwiComparators {

    public static Comparator<Instant> comparingInstantTruncatedToMillis() {
        return comparingInstantTruncatedTo(ChronoUnit.MILLIS);
    }

    public static Comparator<Instant> comparingInstantTruncatedTo(ChronoUnit unit) {
        return Comparator.comparing(i -> i.truncatedTo(unit));
    }

    public static Comparator<LocalDateTime> comparingLocalDateTimeTruncatedToMillis() {
        return comparingLocalDateTimeTruncatedTo(ChronoUnit.MILLIS);
    }

    public static Comparator<LocalDateTime> comparingLocalDateTimeTruncatedTo(ChronoUnit unit) {
        return Comparator.comparing(ldt -> ldt.truncatedTo(unit));
    }

    public static Comparator<ZonedDateTime> comparingZonedDateTimeTruncatedToMillis() {
        return comparingZonedDateTimeTruncatedTo(ChronoUnit.MILLIS);
    }

    public static Comparator<ZonedDateTime> comparingZonedDateTimeTruncatedTo(ChronoUnit unit) {
        return Comparator.comparing(zdt -> zdt.truncatedTo(unit));
    }

    public static Comparator<OffsetDateTime> comparingOffsetDateTimeTruncatedToMillis() {
        return comparingOffsetDateTimeTruncatedTo(ChronoUnit.MILLIS);
    }

    public static Comparator<OffsetDateTime> comparingOffsetDateTimeTruncatedTo(ChronoUnit unit) {
        return Comparator.comparing(odt -> odt.truncatedTo(unit));
    }
}
