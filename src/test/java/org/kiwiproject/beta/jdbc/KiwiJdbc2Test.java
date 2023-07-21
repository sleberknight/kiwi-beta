package org.kiwiproject.beta.jdbc;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@DisplayName("KiwiJdbc2")
public class KiwiJdbc2Test {

    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        resultSet = mock(ResultSet.class);
    }

    @Nested
    class ArrayAsList {

        @Test
        void shouldReturnEmptyList_WhenGetArray_ReturnsNull() throws SQLException {
            mockNullArray(resultSet);

            List<String> values = KiwiJdbc2.arrayAsList(resultSet, "some_values", String.class);

            assertThat(values).isEmpty();

            verify(resultSet).getArray("some_values");
        }

        @Test
        void shouldReturnList_WhenGetArray_ReturnsLongObjects() throws SQLException {
            var values = new Long[] { 42L, 142L, 442L, 4042L };

            mockArray(resultSet, values);

            List<Long> luckyNumbers = KiwiJdbc2.arrayAsList(resultSet, "lucky_numbers", Long.class);

            assertThat(luckyNumbers).containsExactly(values);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldReturnList_WhenGetArray_ReturnsPrimitiveLongs() throws SQLException {
            var values = new long[] { 42L, 142L, 442L, 4042L };

            mockArray(resultSet, values);

            List<Long> luckyNumbers = KiwiJdbc2.arrayAsList(resultSet, "lucky_numbers", Long.class);

            var expectedValues = Arrays.stream(values).boxed().collect(toList());
            assertThat(luckyNumbers).containsExactlyElementsOf(expectedValues);

            verify(resultSet).getArray("lucky_numbers");
        }
    }

    @Nested
    class ArrayAsSet {

        @Test
        void shouldReturnEmptySet_WhenGetArray_ReturnsNull() throws SQLException {
            mockNullArray(resultSet);

            Set<String> values = KiwiJdbc2.arrayAsSet(resultSet, "some_values", String.class);

            assertThat(values).isEmpty();

            verify(resultSet).getArray("some_values");
        }

        @Test
        void shouldReturnSet_WhenGetArray_ReturnsLongObjects() throws SQLException {
            var values = new Long[] { 42L, 142L, 442L, 4042L };

            mockArray(resultSet, values);

            Set<Long> luckyNumbers = KiwiJdbc2.arrayAsSet(resultSet, "lucky_numbers", Long.class);

            assertThat(luckyNumbers).isEqualTo(Sets.newHashSet(values));

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldReturnSet_WhenGetArray_ReturnsPrimitiveInts() throws SQLException {
            var values = new int[] { 42, 142, 442, 4042 };

            mockArray(resultSet, values);

            Set<Integer> luckyNumbers = KiwiJdbc2.arrayAsSet(resultSet, "lucky_numbers", Integer.class);

            var expectedValues = Arrays.stream(values).boxed().collect(toSet());
            assertThat(luckyNumbers).containsExactlyInAnyOrderElementsOf(expectedValues);

            verify(resultSet).getArray("lucky_numbers");
        }
    }

    @Nested
    class DoubleArray {

        @Test
        void shouldReturnEmptyArray_WhenGetArray_ReturnsNull() throws SQLException {
            mockNullArray(resultSet);

            double[] values = KiwiJdbc2.doubleArray(resultSet, "some_values");

            assertThat(values).isEmpty();

            verify(resultSet).getArray("some_values");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsDoubleObjects() throws SQLException {
            var values = new Double[] { 42.0, 342.0, 142.0, 42.0, 84.0 };
            mockArray(resultSet, values);

            double[] luckyNumbers = KiwiJdbc2.doubleArray(resultSet, "lucky_numbers");

            var expectedValues = Arrays.stream(values).mapToDouble(Double::doubleValue).toArray();
            assertThat(luckyNumbers).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveDoubles() throws SQLException {
            var values = new double[] { 42.0, 342.0, 142.0, 42.0, 84.0 };
            mockArray(resultSet, values);

            double[] luckyNumbers = KiwiJdbc2.doubleArray(resultSet, "lucky_numbers");

            assertThat(luckyNumbers).isEqualTo(values);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldThrowIllegalState_WhenGetArray_ReturnsInvalidType() throws SQLException {
            var values = new String[] { "foo", "bar", "baz" };
            mockArray(resultSet, values);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiJdbc2.doubleArray(resultSet, "unlucky_numbers"))
                    .withMessage("expected array of double or java.lang.Double in column 'unlucky_numbers' but found array of: class java.lang.String");
        }
    }

    @Nested
    class IntArray {

        @Test
        void shouldReturnEmptyArray_WhenGetArray_ReturnsNull() throws SQLException {
            mockNullArray(resultSet);

            int[] values = KiwiJdbc2.intArray(resultSet, "some_values");

            assertThat(values).isEmpty();

            verify(resultSet).getArray("some_values");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsIntegerObjects() throws SQLException {
            var values = new Integer[] { 42, 342, 142, 42, 84 };
            mockArray(resultSet, values);

            int[] luckyNumbers = KiwiJdbc2.intArray(resultSet, "lucky_numbers");

            var expectedValues = Arrays.stream(values).mapToInt(Integer::intValue).toArray();
            assertThat(luckyNumbers).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveInts() throws SQLException {
            var values = new int[] { 42, 342, 142, 42, 84 };
            mockArray(resultSet, values);

            int[] luckyNumbers = KiwiJdbc2.intArray(resultSet, "lucky_numbers");

            assertThat(luckyNumbers).isEqualTo(values);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldThrowIllegalState_WhenGetArray_ReturnsInvalidType() throws SQLException {
            var values = new String[] { "foo", "bar", "baz" };
            mockArray(resultSet, values);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiJdbc2.intArray(resultSet, "unlucky_numbers"))
                    .withMessage("expected array of int or java.lang.Integer in column 'unlucky_numbers' but found array of: class java.lang.String");
        }
    }

    @Nested
    class LongArray {

        @Test
        void shouldReturnEmptyArray_WhenGetArray_ReturnsNull() throws SQLException {
            mockNullArray(resultSet);

            long[] values = KiwiJdbc2.longArray(resultSet, "some_values");

            assertThat(values).isEmpty();

            verify(resultSet).getArray("some_values");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsLongObjects() throws SQLException {
            var values = new Long[] { 42L, 342L, 142L, 42L, 84L };
            mockArray(resultSet, values);

            long[] luckyNumbers = KiwiJdbc2.longArray(resultSet, "lucky_numbers");

            var expectedValues = Arrays.stream(values).mapToLong(Long::longValue).toArray();
            assertThat(luckyNumbers).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveLongs() throws SQLException {
            var values = new long[] { 42L, 342L, 142L, 42L, 84L };
            mockArray(resultSet, values);

            long[] luckyNumbers = KiwiJdbc2.longArray(resultSet, "lucky_numbers");

            assertThat(luckyNumbers).isEqualTo(values);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldThrowIllegalState_WhenGetArray_ReturnsInvalidType() throws SQLException {
            var values = new String[] { "foo", "bar", "baz" };
            mockArray(resultSet, values);

            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiJdbc2.longArray(resultSet, "unlucky_numbers"))
                    .withMessage("expected array of long or java.lang.Long in column 'unlucky_numbers' but found array of: class java.lang.String");
        }
    }

    @Nested
    class ArrayMethod {

        @Test
        void shouldThrowIllegalArgument_WhenGivenPrimitiveType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiJdbc2.array(resultSet, "some_values", Integer.TYPE))
                    .withMessage("primitive types are not supported; use a wrapper type or a dedicated primitive method");
        }

        @Test
        void shouldReturnEmptyArray_WhenGetArray_ReturnsNull() throws SQLException {
            mockNullArray(resultSet);

            String[] values = KiwiJdbc2.array(resultSet, "some_values", String.class);

            assertThat(values).isEmpty();

            verify(resultSet).getArray("some_values");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsReferenceType() throws SQLException {
            var values = new Integer[] { 42, 42, 84 };
            mockArray(resultSet, values);

            var luckyNumbers = KiwiJdbc2.array(resultSet, "lucky_numbers", Integer.class);

            assertThat(luckyNumbers).isEqualTo(values);

            verify(resultSet).getArray("lucky_numbers");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveBooleans() throws SQLException {
            var values = new boolean[] { true, true, false, true, false };
            mockArray(resultSet, values);

            var luckyBooleans = KiwiJdbc2.array(resultSet, "lucky_bools", Boolean.class);

            Boolean[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyBooleans).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_bools");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveBytes() throws SQLException {
            var values = new byte[] { 1, 4, 0, 3, 2 };
            mockArray(resultSet, values);

            var luckyBytes = KiwiJdbc2.array(resultSet, "lucky_bytes", Byte.class);

            Byte[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyBytes).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_bytes");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveChars() throws SQLException {
            var values = new char[] { 'a', 'd', 'c', 'a' };
            mockArray(resultSet, values);

            var luckyChars = KiwiJdbc2.array(resultSet, "lucky_chars", Character.class);

            Character[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyChars).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_chars");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveDoubles() throws SQLException {
            var values = new double[] { 42.0, 342.0, 142.0, 42.0, 84.0 };
            mockArray(resultSet, values);

            var luckyDoubles = KiwiJdbc2.array(resultSet, "lucky_doubles", Double.class);

            Double[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyDoubles).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_doubles");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveFloats() throws SQLException {
            var values = new float[] { 42.0F, 342.0F, 142.0F, 42.0F, 84.0F };
            mockArray(resultSet, values);

            var luckyFloats = KiwiJdbc2.array(resultSet, "lucky_floats", Float.class);

            Float[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyFloats).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_floats");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveInts() throws SQLException {
            var values = new int[] { 42, 342, 142, 42, 84 };
            mockArray(resultSet, values);

            var luckyInts = KiwiJdbc2.array(resultSet, "lucky_ints", Integer.class);

            Integer[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyInts).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_ints");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveLongs() throws SQLException {
            var values = new long[] { 42L, 342L, 142L, 42L, 84L };
            mockArray(resultSet, values);

            var luckyLongs = KiwiJdbc2.array(resultSet, "lucky_longs", Long.class);

            Long[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyLongs).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_longs");
        }

        @Test
        void shouldReturnArray_WhenGetArray_ReturnsPrimitiveShorts() throws SQLException {
            var values = new short[] { 42, 342, 142, 42, 84 };
            mockArray(resultSet, values);

            var luckyShorts = KiwiJdbc2.array(resultSet, "lucky_shorts", Short.class);

            Short[] expectedValues = ArrayUtils.toObject(values);
            assertThat(luckyShorts).isEqualTo(expectedValues);

            verify(resultSet).getArray("lucky_shorts");
        }
    }

    private static void mockNullArray(ResultSet rs) throws SQLException {
        when(rs.getArray(anyString())).thenReturn(null);
    }

    private static void mockArray(ResultSet rs, Object actualArray) throws SQLException {
        var array = newMockArray(actualArray);
        when(rs.getArray(anyString())).thenReturn(array);
    }

    private static Array newMockArray(Object actualArray) throws SQLException {
        var array = mock(Array.class);
        when(array.getArray()).thenReturn(actualArray);
        return array;
    }

    @Nested
    class PrimitiveToObjectArray {

        @Test
        void shouldThrowIllegalArgument_WhenArgumentIsNotAnArray() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiJdbc2.primitiveToObjectArray("this is not an array"))
                    .withMessage("primitiveArray must be an array of a primitive type");
        }

        @Test
        void shouldThrowIllegalArgument_WhenArgumentIsNotAnArrayOfPrimitives() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiJdbc2.primitiveToObjectArray(new Integer[] { 42, 84}))
                    .withMessage("primitiveArray must be an array of a primitive type");
        }

        @Test
        void shouldThrowIllegalState_WhenComponentTypeIsNotPrimitive() {
            var ints = new int[] { 24, 42, 84 };
            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiJdbc2.primitiveToObjectArray(ints, String.class))
                    .withMessage("expected array to be short[] since it is not any other primitive type, but was: class java.lang.String");
        }
    }
}
