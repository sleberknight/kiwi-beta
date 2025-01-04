package org.kiwiproject.beta.util.function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@DisplayName("CountingPredicate")
class CountingPredicateTest {

    @Test
    void shouldRequireNonNullPredicate() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new CountingPredicate<>(null));
    }

    @Test
    void shouldHaveZeroInitialCounts() {
        var isOdd = new CountingPredicate<Integer>(value -> value % 2 != 0);

        assertThat(isOdd.trueCount()).isZero();
        assertThat(isOdd.falseCount()).isZero();
    }

    @ParameterizedTest
    @MethodSource("collectionsOfIntFactory")
    void shouldTrackCountsInCollections(Collection<Integer> collection) {
        var isEven = new CountingPredicate<Integer>(value -> value % 2 == 0);

        //noinspection ResultOfMethodCallIgnored
        collection.stream().filter(isEven).toList();

        assertThat(isEven.trueCount()).isEqualTo(4);
        assertThat(isEven.falseCount()).isEqualTo(5);
    }

    @Test
    void shouldTrackCountsInArrays() {
        var isOdd = new CountingPredicate<Integer>(value -> value % 2 != 0);

        Integer[] numbers = { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        //noinspection ResultOfMethodCallIgnored
        Arrays.stream(numbers).filter(isOdd).toList();

        assertThat(isOdd.trueCount()).isEqualTo(5);
        assertThat(isOdd.falseCount()).isEqualTo(4);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void shouldAccumulateCounts_WhenPredicateIsUsedMultipleTimes() {
        var ints1 = List.of(1, 2, 3, 4);
        var ints2 = Set.of(5, 6, 7, 8);
        var ints3 = List.of(9);
        var ints4 = Set.of(10, 11, 12);
        var ints5 = List.of(15);

        var isEven = new CountingPredicate<Integer>(value -> value % 2 == 0);

        ints1.stream().filter(isEven).toList();
        ints2.stream().filter(isEven).toList();
        ints3.stream().filter(isEven).toList();
        ints4.stream().filter(isEven).toList();
        ints5.stream().filter(isEven).toList();

        assertThat(isEven.trueCount()).isEqualTo(6);
        assertThat(isEven.falseCount()).isEqualTo(7);
    }

    static Stream<Collection<Integer>> collectionsOfIntFactory() {
        return Stream.of(
                List.of(1, 2, 3, 4, 5, 6, 7, 8, 9),
                Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
        );
    }
}
