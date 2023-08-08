package org.kiwiproject.beta.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;
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

    @Nested
    class PrimitiveToObjectArray {

        @Test
        void shouldThrowIllegalArgument_WhenPrimitiveArrayArgumentIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiArrays2.primitiveToObjectArray(null, Character.class))
                    .withMessage("primitiveArray must be not be null");
        }

        @Test
        void shouldThrowIllegalArgument_WhenWrapperTypeArgumentIsNull() {
            var array = new byte[] {};
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiArrays2.primitiveToObjectArray(array, null))
                    .withMessage("wrapperType must not be null");
        }

        @Test
        void shouldThrowIllegalArgument_WhenWrapperTypeArgumentIsNotPrimitiveWrapperType() {
            var array = new byte[] {};
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiArrays2.primitiveToObjectArray(array, String.class))
                    .withMessage("wrapperType must be a primitive wrapper type");
        }

        @Test
        void shouldThrowIllegalArgument_WhenPrimitiveArrayArgumentIsNotAnArray() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiArrays2.primitiveToObjectArray("this is not an array", Byte.class))
                    .withMessage("primitiveArray must be an array of a primitive type");
        }

        @Test
        void shouldThrowIllegalArgument_WhenPrimitiveArrayArgumentIsNotAnArrayOfPrimitives() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiArrays2.primitiveToObjectArray(new Integer[] { 42, 84}, Integer.class))
                    .withMessage("primitiveArray must be an array of a primitive type");
        }

        @Test
        void shouldConvert_PrimitiveBooleanArrays() {
            var values = new boolean[] { true, true, false, true, false };

            var booleanObjArray = KiwiArrays2.primitiveToObjectArray(values, Boolean.class);

            Boolean[] expectedValues = ArrayUtils.toObject(values);
            assertThat(booleanObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvert_PrimitiveByteArrays() {
            var values = new byte[] { 1, 4, 0, 3, 2 };

            var byteObjArray = KiwiArrays2.primitiveToObjectArray(values, Byte.class);

            Byte[] expectedValues = ArrayUtils.toObject(values);
            assertThat(byteObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvert_PrimitiveCharArrays() {
            var values = new char[] { 'a', 'd', 'c', 'a' };

            var characterObjArray = KiwiArrays2.primitiveToObjectArray(values, Character.class);

            Character[] expectedValues = ArrayUtils.toObject(values);
            assertThat(characterObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvert_PrimitiveDoubleArrays() {
            var values = new double[] { 42.0, 342.0, 142.0, 42.0, 84.0 };

            var doubleObjArray = KiwiArrays2.primitiveToObjectArray(values, Double.class);

            Double[] expectedValues = ArrayUtils.toObject(values);
            assertThat(doubleObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvert_PrimitiveFloatArrays() {
            var values = new float[] { 42.0F, 342.0F, 142.0F, 42.0F, 84.0F };

            var floatObjArray = KiwiArrays2.primitiveToObjectArray(values, Float.class);

            Float[] expectedValues = ArrayUtils.toObject(values);
            assertThat(floatObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvert_PrimitiveIntArrays() {
            var values = new int[] { 42, 342, 142, 42, 84 };

            var integerObjArray = KiwiArrays2.primitiveToObjectArray(values, Integer.class);

            Integer[] expectedValues = ArrayUtils.toObject(values);
            assertThat(integerObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvert_PrimitiveLongArrays() {
            var values = new long[] { 42L, 342L, 142L, 42L, 84L };

            var longObjArray = KiwiArrays2.primitiveToObjectArray(values, Long.class);

            Long[] expectedValues = ArrayUtils.toObject(values);
            assertThat(longObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvert_PrimitiveShortArrays() {
            var values = new short[] { 42, 342, 142, 42, 84 };

            var shortObjArray = KiwiArrays2.primitiveToObjectArray(values, Short.class);

            Short[] expectedValues = ArrayUtils.toObject(values);
            assertThat(shortObjArray).isEqualTo(expectedValues);
        }

        @Test
        void shouldConvertEmptyArrays() {
            var values = new int[0];

            var integerObjArray = KiwiArrays2.primitiveToObjectArray(values, Integer.class);

            assertThat(integerObjArray).isEmpty();
        }
    }

    @Nested
    class PrimitiveToObjectArrayOrNull {

        @Test
        void shouldReturnNull_WhenArgumentIsNull() {
            byte[] bytes = null;
            //noinspection ConstantValue
            Byte[] result = KiwiArrays2.primitiveToObjectArrayOrNull(bytes, Byte.class);
            assertThat(result).isNull();
        }

        @Test
        void shouldConvertToObjectArray() {
            var values = new long[] { 42L, 342L, 142L, 42L, 84L };

            var longObjectArray = KiwiArrays2.primitiveToObjectArrayOrNull(values, Long.class);

            Long[] expectedValues = ArrayUtils.toObject(values);
            assertThat(longObjectArray).isEqualTo(expectedValues);
        }
    }

    @Nested
    class PrimitiveToObjectArrayOrEmpty {

        @Test
        void shouldReturnEmptyOptional_WhenArgumentIsNull() {
            byte[] bytes = null;
            //noinspection ConstantValue
            Optional<Byte[]> result = KiwiArrays2.primitiveToObjectArrayOrEmpty(bytes, Byte.class);
            assertThat(result).isEmpty();
        }

        @Test
        void shouldConvertToObjectArray() {
            var values = new int[] { 42, 342, 142, 42, 84 };

            var integerObjectArrayOpt = KiwiArrays2.primitiveToObjectArrayOrEmpty(values, Integer.class);

            Integer[] expectedValues = ArrayUtils.toObject(values);
            assertThat(integerObjectArrayOpt).contains(expectedValues);
        }
    }
}
