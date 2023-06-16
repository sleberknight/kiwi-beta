package org.kiwiproject.beta.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

@DisplayName("KiwiArrays2")
class KiwiArrays2Test {

    @Test
    void emptyArray_withNullType_ShouldThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiArrays2.emptyArray(null));
    }

    // This exists only to have the concrete type declared (the parameterized method below tests multiple generic types)
    @Test
    void emptyArray_shouldReturnEmptyArray() {
        Integer[] result = KiwiArrays2.emptyArray(Integer.class);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(classes = { Integer.class, String.class, Boolean.class })
    <T> void emptyArray_shouldReturnEmptyArray(Class<T> type) {
        T[] result = KiwiArrays2.emptyArray(type);

        assertThat(result).isEmpty();
    }

    @Test
    void newArray_withNullTypeAndValidLength_ShouldThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiArrays2.newArray(null, 10));
    }

    // This exists only to have the concrete type declared (the parameterized method below tests multiple generic types)
    @Test
    void newArray_shouldReturnArrayWithSpecifiedLength() {
        Long[] result = KiwiArrays2.newArray(Long.class, 5);

        assertThat(result).hasSize(5).containsOnlyNulls();
    }

    @Test
    void newArray_shouldReturnArrayWithSpecifiedLength_OfZero() {
        String[] result = KiwiArrays2.newArray(String.class, 0);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("newArrayTypeAndLengthProvider")
    <T> void newArray_shouldReturnArrayWithSpecifiedLength(Class<T> type, int length) {
        T[] result = KiwiArrays2.newArray(type, length);

        assertThat(result).hasSize(length).containsOnlyNulls();
    }

    // This exists only to have the concrete type declared (the parameterized method below tests multiple generic types)
    @Test
    void newArray_withNegativeLength_shouldThrowIllegalArgumentException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiArrays2.newArray(Double.class, -1));
    }

    @ParameterizedTest
    @MethodSource("newArrayNegativeLengthProvider")
    void newArray_withNegativeLength_shouldThrowIllegalArgumentException(Class<?> type, int length) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiArrays2.newArray(type, length));
    }

    static Stream<Arguments> newArrayTypeAndLengthProvider() {
        return Stream.of(
                Arguments.of(Integer.class, 5),
                Arguments.of(String.class, 10),
                Arguments.of(Boolean.class, 25)
        );
    }

    static Stream<Arguments> newArrayNegativeLengthProvider() {
        return Stream.of(
                Arguments.of(Double.class, -1),
                Arguments.of(Character.class, -5),
                Arguments.of(String.class, -42)
        );
    }
}
