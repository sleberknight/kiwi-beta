package org.kiwiproject.beta.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ClassUtils.isPrimitiveWrapper;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Array;
import java.util.Optional;

/**
 * Utilities related to arrays.
 * <p>
 * These utilities can be considered for inclusion into kiwi's {@link org.kiwiproject.collect.KiwiArrays} class.
 */
@UtilityClass
@Beta
public class KiwiArrays2 {

    /**
     * Creates an empty array of the specified type.
     *
     * @param <T>  the type parameter representing the component type of the array
     * @param type the class object representing the component type of the array
     * @return an empty array of the specified type
     * @throws IllegalArgumentException if type is null or is {@link Void#TYPE}
     * @implNote This method exists because {@link Array#newInstance(Class, int)} returns Object and thus
     * requires a cast. Using this method, code can be a little cleaner without a cast.
     * @see Array#newInstance(Class, int)
     */
    public static <T> T[] emptyArray(Class<T> type) {
        return newArray(type, 0);
    }

    /**
     * Creates a new array of the specified type and length. All values in the array are null.
     *
     * @param <T>    the type parameter representing the component type of the array
     * @param type   the class object representing the component type of the array
     * @param length the length of the new array
     * @return a new array of the specified type and length
     * @throws IllegalArgumentException if type is null or is {@link Void#TYPE}, or length is negative
     * @implNote This method exists because {@link Array#newInstance(Class, int)} returns Object and thus
     * requires a cast. Using this method, code can be a little cleaner without a cast.
     * @see Array#newInstance(Class, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] newArray(Class<T> type, int length) {
        checkArgumentNotNull(type);
        checkArgument(length >= 0, "value must be positive or zero");
        return (T[]) Array.newInstance(type, length);
    }

    /**
     * Convert an array of primitives to an array of the corresponding wrapper type.
     *
     * @param primitiveArray an array of primitives
     * @param wrapperType the wrapper type of the primitive type
     * @param <T> the wrapper type corresponding to the input array's primitive type
     * @return an array of the wrapper type, or {@code null} if the input array is null
     * @throws IllegalArgumentException if the input array is not an array of primitives, or the wrapper type
     * is not a primitive wrapper class
     * @implNote Intentionally suppressing Sonar java:S1168 (Return an empty array instead of null) because
     * the method is explicit that it returns null when given null input.
     */
    @Nullable
    @SuppressWarnings("java:S1168")
    public static <T> T[] primitiveToObjectArrayOrNull(@Nullable Object primitiveArray, Class<T> wrapperType) {
        if (isNull(primitiveArray)) {
            return null;
        }

        return primitiveToObjectArray(primitiveArray, wrapperType);
    }

    /**
     * Convert an array of primitives to an array of the corresponding wrapper type.
     *
     * @param primitiveArray an array of primitives
     * @param wrapperType the wrapper type of the primitive type
     * @param <T> the wrapper type corresponding to the input array's primitive type
     * @return an Optional containing an array of the wrapper type, or an empty Optional if the input array is null
     * @throws IllegalArgumentException if the input array is not an array of primitives or the wrapper type
     * is not a primitive wrapper class
     */
    public static <T> Optional<T[]> primitiveToObjectArrayOrEmpty(@Nullable Object primitiveArray, Class<T> wrapperType) {
        if (isNull(primitiveArray)) {
            return Optional.empty();
        }

        return Optional.of(primitiveToObjectArray(primitiveArray, wrapperType));
    }

	/**
     * Convert an array of primitives to an array of the corresponding wrapper type.
     *
	 * @param primitiveArray an array of primitives
     * @param wrapperType the wrapper type of the primitive type
	 * @param <T> the wrapper type corresponding to the input array's primitive type
	 * @return an array of the wrapper type
     * @throws IllegalArgumentException if the input array is null or is not an array of primitives or the wrapper type
     * is not a primitive wrapper class
     * @implNote The internal logic is not pretty, but I cannot find an existing utility that does this (e.g., in
     * Apache Commons or Google Guava). Apache Commons Lang's {@code ArrayUtils} is used internally to convert
     * primitive arrays, but because of needing to handle all Java primitive types, I can't think of a cleaner way to
     * do this other than a conditional covering all eight primitive types.
	 */
    @SuppressWarnings({"unchecked"})
	public static <T> T[] primitiveToObjectArray(Object primitiveArray, Class<T> wrapperType) {
        checkArgumentNotNull(primitiveArray, "primitiveArray must be not be null");
        checkArgumentNotNull(wrapperType, "wrapperType must not be null");
        checkArgument(isPrimitiveWrapper(wrapperType), "wrapperType must be a primitive wrapper type");

        var arrayClass = primitiveArray.getClass();
	    var componentType = arrayClass.getComponentType();
	    checkArgument(arrayClass.isArray() && componentType.isPrimitive(),
	            "primitiveArray must be an array of a primitive type");

        if (componentType.equals(Boolean.TYPE)) {
            var primitiveBooleanArray = (boolean[]) primitiveArray;
            return (T[]) ArrayUtils.toObject(primitiveBooleanArray);
        } else if (componentType.equals(Byte.TYPE)) {
            var primitiveByteArray = (byte[]) primitiveArray;
            return (T[]) ArrayUtils.toObject(primitiveByteArray);
        } else if (componentType.equals(Character.TYPE)) {
            var primitiveCharArray = (char[]) primitiveArray;
            return (T[]) ArrayUtils.toObject(primitiveCharArray);
        } else if (componentType.equals(Double.TYPE)) {
            var primitiveDoubleArray = (double[]) primitiveArray;
            return (T[]) ArrayUtils.toObject(primitiveDoubleArray);
        } else if (componentType.equals(Float.TYPE)) {
            var primitiveFloatArray = (float[]) primitiveArray;
            return (T[]) ArrayUtils.toObject(primitiveFloatArray);
        } else if (componentType.equals(Integer.TYPE)) {
            var primitiveIntArray = (int[]) primitiveArray;
            return (T[]) ArrayUtils.toObject(primitiveIntArray);
        } else if (componentType.equals(Long.TYPE)) {
            var primitiveLongArray = (long[]) primitiveArray;
            return (T[]) ArrayUtils.toObject(primitiveLongArray);
        }

        // it should be a short[] since we've checked the other seven primitive types above; this should
        // never throw unless a new primitive type is added (exceedingly unlikely) or the above code is
        // modified such that it doesn't properly check the component type, or doesn't handle all primitive
        // types, etc. In short, only a programming error.
        checkState(primitiveArray instanceof short[],
                "expected array to be short[] since it is not any other primitive type, but was: %s",
                componentType);

        var primitiveShortArray = (short[]) primitiveArray;
        return  (T[]) ArrayUtils.toObject(primitiveShortArray);
	}
}
