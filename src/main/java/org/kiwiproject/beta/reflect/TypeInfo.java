package org.kiwiproject.beta.reflect;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.collect.KiwiLists.first;

import com.google.common.annotations.VisibleForTesting;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents either a simple type (e.g. Boolean or String) or a parameterized type (e.g. {@code List<String>}
 * or {@code Map<String, Integer>}).
 */
@Value
public class TypeInfo {

    /**
     * If a simple type, this is the entirety of the type information. If a parameterized type, this is the
     * top-level type, e.g. List or Map.
     */
    Type rawType;

    /**
     * If a simple type, this will be empty. If a parameterized type, then this list contains the generic types.
     * For example, a {@code List<String>} has one generic type, String, while a {@code Map<String, Integer>}
     * contains two generic types, String and Integer.
     */
    List<Type> genericTypes;

    /**
     * Package-private constructor to allow for testing, but not direct instantiation. Users of this class
     * should use the provided factory methods.
     */
    @VisibleForTesting
    TypeInfo(Type rawType, List<Type> genericTypes) {
        this.rawType = requireNotNull(rawType);
        this.genericTypes = requireNotNull(genericTypes);
    }

    /**
     * Create a new instance representing either a simple type or a parameterized type.
     *
     * @param type the type, e.g. String, {@code List<Integer}, or {@code Map<String, Integer}
     * @return a new instance
     * @see TypeInfo#ofSimpleType(Type)
     * @see TypeInfo#ofParameterizedType(ParameterizedType)
     */
    public static TypeInfo ofType(@NonNull Type type) {
        checkTypeArgument(type);

        if (type instanceof ParameterizedType) {
            return TypeInfo.ofParameterizedType((ParameterizedType) type);
        }

        return TypeInfo.ofSimpleType(type);
    }

    /**
     * Create a new instance representing a simple type such as Integer or String, or raw collections.
     *
     * @param simpleType the type, e.g. String, but also can represent a raw Collection, Map, etc.
     * @return a new instance with the given raw type and an empty list of generic types
     */
    public static TypeInfo ofSimpleType(@NonNull Type simpleType) {
        checkTypeArgument(simpleType);

        // TODO Should we consider an illegal argument if simpleType is actually a ParameterizedType?

        return new TypeInfo(simpleType, List.of());
    }

    /**
     * Create a new instance representing a parameterized type such as {@code List<String>}
     * or {@code Map<String, Integer>}.
     *
     * @param parameterizedType the parameterized type, e.g. {@code List<Integer>}, {@code Map<String, Object>},
     * or {@code Set<String>}
     * @return a new instance
     */
    public static TypeInfo ofParameterizedType(@NonNull ParameterizedType parameterizedType) {
        checkTypeArgument(parameterizedType);

        var typeArgs = parameterizedType.getActualTypeArguments();
        List<Type> genericTypes = isNull(typeArgs) ? List.of() : List.of(typeArgs);

        return new TypeInfo(parameterizedType.getRawType(), genericTypes);
    }

    private static void checkTypeArgument(Type type) {
        checkArgumentNotNull(type, "type to inspect must not be null");
    }

    /**
     * Check if this is a collection (Collection, Set, List).
     *
     * @return true if assignable to Collection, otherwise false
     */
    public boolean isCollection() {
        return isRawTypeAssignableTo(Collection.class);
    }

    /**
     * Check if this is a map.
     *
     * @return true if assignable to Map, otherwise false
     */
    public boolean isMap() {
        return isRawTypeAssignableTo(Map.class);
    }

    /**
     * Check if the raw type is exactly the same as {@code testType}.
     *
     * @param testType the type to test against, e.g. Boolean or String
     * @return true if this has the exact raw type given, otherwise false
     */
    public boolean hasExactRawType(@NonNull Class<?> testType) {
        checkArgumentNotNull(testType);
        return rawType.equals(testType);
    }

    /**
     * Check if the raw type is assignable to the {@code testType}, for example a raw type of ArrayList
     * is assignable to List, and a String is assignable to a CharSequence.
     *
     * @param testType the type to test against, e.g. Collection, Map, CharSequence
     * @return true if assignable to the given test type, otherwise false
     */
    public boolean hasRawTypeAssignableTo(@NonNull Class<?> testType) {
        checkArgumentNotNull(testType);
        return isRawTypeAssignableTo(testType);
    }

    private boolean isRawTypeAssignableTo(Class<?> testType) {
        var clazz = getClassForName(rawType.getTypeName());
        return testType.isAssignableFrom(clazz);
    }

    @VisibleForTesting
    static Class<?> getClassForName(String typeName) {
        try {
            return Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Assumes this represents a Collection, and returns the single generic type, e.g. for a {@code Set<Integer>}
     * then Integer is returned. Throws an exception if this does not represent a Collection with one generic type.
     *
     * @return the generic type of a Collection
     * @throws IllegalStateException if this does not have exactly one generic type
     */
    public Type getOnlyGenericType() {
        var genericTypeCount = genericTypes.size();
        checkState(genericTypeCount == 1,
                "expected exactly one generic type but found %s", genericTypeCount);
        return first(genericTypes);
    }
}
