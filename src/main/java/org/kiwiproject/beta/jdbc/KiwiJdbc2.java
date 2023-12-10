package org.kiwiproject.beta.jdbc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;
import org.kiwiproject.beta.collect.KiwiArrays2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * JDBC utilities that may (or may not) eventually move into {@code KiwiJdbc} in
 * <a href="https://github.com/kiwiproject/kiwi">kiwi</a>.
 */
@UtilityClass
@Beta
public class KiwiJdbc2 {

    /**
     * Get an ARRAY column as a List of the specified type.
     *
     * @param rs the ResultSet
     * @param columnLabel the name of the array column in the ResultSet
     * @param type the data type
     * @param <T> type parameter for the data type
     * @return a list containing the values from the array, <em>or an empty list if the database value is null or empty</em>
     * @throws SQLException if a database error occurred, e.g. the column doesn't exist
     * @see java.sql.Array
     * @see java.sql.ResultSet#getArray(String)
     */
    public static <T> List<T> arrayAsList(ResultSet rs, String columnLabel, Class<T> type) throws SQLException {
        T[] ts = array(rs, columnLabel, type);
        return Lists.newArrayList(ts);
    }

    /**
     * Get an ARRAY column as a Set of the specified type.
     *
     * @param rs the ResultSet
     * @param columnLabel the name of the array column in the ResultSet
     * @param type the data type
     * @param <T> type parameter for the data type
     * @return a set containing the values from the array, <em>or an empty set if the database value is null or empty</em>
     * @throws SQLException if a database error occurred, e.g. the column doesn't exist
     * @see java.sql.Array
     * @see java.sql.ResultSet#getArray(String)
     */
    public static <T> Set<T> arrayAsSet(ResultSet rs, String columnLabel, Class<T> type) throws SQLException {
        T[] ts = array(rs, columnLabel, type);
        return Sets.newHashSet(ts);
    }

    /**
     * Get an ARRAY column as a double array.
     *
     * @param rs the ResultSet
     * @param columnLabel the name of the array column in the ResultSet
     * @return an array of double containing the values from the array, <em>or an empty array if the database value is null or empty</em>
     * @throws SQLException if a database error occurred, e.g. the column doesn't exist
     * @see java.sql.Array
     * @see java.sql.ResultSet#getArray(String)
     */
    public static double[] doubleArray(ResultSet rs, String columnLabel) throws SQLException {
        var javaSqlArray = rs.getArray(columnLabel);

        if (isNull(javaSqlArray)) {
            return new double[0];
        }

        var array = javaSqlArray.getArray();
        var clazz = array.getClass();
        checkIsArray(clazz, columnLabel);

        var componentType = clazz.getComponentType();
        checkArrayType(componentType, columnLabel, Double.TYPE, Double.class);

        if (componentType.equals(Double.TYPE)) {
            return (double[]) array;
        }

        var doubleObjArray = (Double[]) array;
        return Arrays.stream(doubleObjArray).mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Get an ARRAY column as an int array.
     *
     * @param rs the ResultSet
     * @param columnLabel the name of the array column in the ResultSet
     * @return an array of int containing the values from the array, <em>or an empty array if the database value is null or empty</em>
     * @throws SQLException if a database error occurred, e.g. the column doesn't exist
     * @see java.sql.Array
     * @see java.sql.ResultSet#getArray(String)
     */
    public static int[] intArray(ResultSet rs, String columnLabel) throws SQLException {
        var javaSqlArray = rs.getArray(columnLabel);

        if (isNull(javaSqlArray)) {
            return new int[0];
        }

        var array = javaSqlArray.getArray();
        var clazz = array.getClass();
        checkIsArray(clazz, columnLabel);

        var componentType = clazz.getComponentType();
        checkArrayType(componentType, columnLabel, Integer.TYPE, Integer.class);

        if (componentType.equals(Integer.TYPE)) {
            return (int[]) array;
        }

        var intObjArray = (Integer[]) array;
        return Arrays.stream(intObjArray).mapToInt(Integer::intValue).toArray();
    }

    /**
     * Get an ARRAY column as a long array.
     *
     * @param rs the ResultSet
     * @param columnLabel the name of the array column in the ResultSet
     * @return an array of long containing the values from the array, <em>or an empty array if the database value is null or empty</em>
     * @throws SQLException if a database error occurred, e.g. the column doesn't exist
     * @see java.sql.Array
     * @see java.sql.ResultSet#getArray(String)
     */
    public static long[] longArray(ResultSet rs, String columnLabel) throws SQLException {
        var javaSqlArray = rs.getArray(columnLabel);

        if (isNull(javaSqlArray)) {
            return new long[0];
        }

        var array = javaSqlArray.getArray();
        var clazz = array.getClass();
        checkIsArray(clazz, columnLabel);

        var componentType = clazz.getComponentType();
        checkArrayType(componentType, columnLabel, Long.TYPE, Long.class);

        if (componentType.equals(Long.TYPE)) {
            return (long[]) array;
        }

        var longObjArray = (Long[]) array;
        return Arrays.stream(longObjArray).mapToLong(Long::longValue).toArray();
    }

    private static <T> void checkArrayType(Class<?> componentType,
                                           String columnLabel,
                                           Class<T> primitiveType,
                                           Class<T> wrapperType) {

        checkState(componentType.equals(primitiveType) || componentType.equals(wrapperType),
                "expected array of %s or %s in column '%s' but found array of: %s",
                primitiveType.getName(), wrapperType.getName(), columnLabel, componentType);
    }

    /**
     * Get an ARRAY column as an array of the specified reference type.
     * <p>
     * If the target type is a primitive double, int, or long, you can specify the wrapper type, or you can
     * instead use one of the specialized primitive methods, e.g. {@link #doubleArray(ResultSet, String)}.
     * Note that if the array returned by {@link java.sql.Array#getArray()} is of a primitive type such as {@code char[]},
     * then this method must convert that array to an array of wrapper objects which adds additional overhead.
     * In those cases, using the specialized primitive methods will result in better performance (for the
     * supported primitive types).
     *
     * @param rs the ResultSet
     * @param columnLabel the name of the array column in the ResultSet
     * @param type the data type (must be a reference type)
     * @param <T> type parameter for the data type
     * @return an array of objects of type T containing the values from the array, or <em>an empty array if the database
     * value is null or empty</em>
     * @throws SQLException if a database error occurred, e.g. the column doesn't exist
     * @see java.sql.Array
     * @see java.sql.ResultSet#getArray(String)
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T[] array(ResultSet rs, String columnLabel, Class<T> type) throws SQLException {
        checkArgument(!type.isPrimitive(),
                "primitive types are not supported; use a wrapper type or a dedicated primitive method");

        var javaSqlArray = rs.getArray(columnLabel);

        if (isNull(javaSqlArray)) {
            return KiwiArrays2.emptyArray(type);
        }

        var array = javaSqlArray.getArray();
        var clazz = array.getClass();
        checkIsArray(clazz, columnLabel);

        if (clazz.getComponentType().isPrimitive()) {
            return KiwiArrays2.primitiveToObjectArray(array, type);
        }

        return (T[]) array;
    }

    private static void checkIsArray(Class<?> clazz, String columnLabel) {
        checkState(clazz.isArray(), "expected an array in column '%s' but found: %s", columnLabel, clazz);
    }
}
