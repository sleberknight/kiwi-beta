package org.kiwiproject.beta.reflect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.collect.KiwiLists.first;
import static org.kiwiproject.collect.KiwiLists.second;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import lombok.Value;
import org.jspecify.annotations.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents either a simple type (e.g., Boolean or String) or a parameterized type (e.g. {@code List<String>}
 * or {@code Map<String, Integer>}).
 */
@Value
@Beta
public class TypeInfo {

    /**
     * If a simple type, this is the entirety of the type information. If a parameterized type, this is the
     * top-level type, e.g., List or Map.
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
     * @param type the type, e.g., String, {@code List<Integer}, or {@code Map<String, Integer}
     * @return a new instance
     * @see TypeInfo#ofSimpleType(Type)
     * @see TypeInfo#ofParameterizedType(ParameterizedType)
     */
    public static TypeInfo ofType(@NonNull Type type) {
        checkTypeArgument(type);

        if (type instanceof ParameterizedType parameterizedType) {
            return TypeInfo.ofParameterizedType(parameterizedType);
        }

        return TypeInfo.ofSimpleType(type);
    }

    /**
     * Create a new instance representing a simple type such as Integer or String, or raw collections.
     *
     * @param simpleType the type, e.g., String, but also can represent a raw Collection, Map, etc.
     * @return a new instance with the given raw type and an empty list of generic types
     */
    public static TypeInfo ofSimpleType(@NonNull Type simpleType) {
        checkTypeArgument(simpleType);
        checkArgument(!(simpleType instanceof ParameterizedType),
                "Do not call this method with a ParameterizedType. Use TypeInfo#ofType or TypeInfo#ofParameterizedType.");

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
        List<Type> genericTypes = List.of(typeArgs);

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
     * Check if this is a collection (Collection, Set, List) that contains elements with the given generic type.
     *
     * @param genericType the exact generic type of the collection
     * @return true if assignable to Collection, and its elements have the exact generic type, otherwise false
     */
    public boolean isCollectionOf(@NonNull Class<?> genericType) {
        checkGenericTypeArgument(genericType);
        return isCollection() && hasOnlyGenericType(genericType);
    }

    /**
     * Check if this is a List that contains elements with the given generic type.
     *
     * @param genericType the exact generic type of the list
     * @return true if assignable to List and its elements have the exact generic type, otherwise false
     */
    public boolean isListOf(@NonNull Class<?> genericType) {
        checkGenericTypeArgument(genericType);
        return isRawTypeAssignableTo(List.class) && hasOnlyGenericType(genericType);
    }

    /**
     * Check if this is a Set that contains elements with the given generic type.
     *
     * @param genericType the exact generic type of the set
     * @return true if assignable to Set and its elements have the exact generic type, otherwise false
     */
    public boolean isSetOf(@NonNull Class<?> genericType) {
        checkGenericTypeArgument(genericType);
        return isRawTypeAssignableTo(Set.class) && hasOnlyGenericType(genericType);
    }

    /**
     * @implNote Assumes this is called after having verified there is exactly one generic type.
     * If this assumption is violated, an IllegalStateException is thrown.
     */
    private boolean hasOnlyGenericType(Class<?> genericType) {
        return getOnlyGenericType().equals(genericType);
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
     * Check if this is a Map that contains entries with the given key and value generic types.
     *
     * @param keyGenericType the exact generic type of the map keys
     * @param valueGenericType the exact generic type of the map values
     * @return true if assignable to Map, and its elements have the exact key/value generic types, otherwise false
     */
    public boolean isMapOf(@NonNull Class<?> keyGenericType, @NonNull Class<?> valueGenericType) {
        checkGenericTypeArgument(keyGenericType);
        checkGenericTypeArgument(valueGenericType);

        return isMap() && first(genericTypes).equals(keyGenericType) && second(genericTypes).equals(valueGenericType);
    }

    private static void checkGenericTypeArgument(Class<?> genericType) {
        checkArgumentNotNull(genericType, "genericType to check must not be null");
    }

    /**
     * Check if the raw type is exactly the same as {@code testType}.
     *
     * @param testType the type to test against, e.g., Boolean or String
     * @return true if this has the exact raw type given, otherwise false
     */
    public boolean hasExactRawType(@NonNull Class<?> testType) {
        checkArgumentNotNull(testType);
        return rawType.equals(testType);
    }

    /**
     * Check if the raw type is assignable to the {@code testType}, for example, a raw type of ArrayList
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
     * Assumes this represents a Collection, and returns the single generic type, e.g., for a {@code Set<Integer>}
     * then Integer is returned. Throws an exception if this does not represent a Collection with one generic type.
     *
     * @return the generic type of the Collection
     * @throws IllegalStateException if this does not have exactly one generic type
     */
    public Type getOnlyGenericType() {
        var genericTypeCount = genericTypes.size();
        checkState(genericTypeCount == 1,
                "expected exactly one generic type but found %s", genericTypeCount);
        return first(genericTypes);
    }
}
