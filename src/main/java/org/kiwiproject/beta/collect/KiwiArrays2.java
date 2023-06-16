package org.kiwiproject.beta.collect;

import static com.google.common.base.Preconditions.checkArgument;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Array;

/**
 * Utilities related to arrays.
 * <p>
 * These utilities can be considered for inclusion into kiwi's {@link org.kiwiproject.collect.KiwiArrays} class.
 */
@UtilityClass
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
}
