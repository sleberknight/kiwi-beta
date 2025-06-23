package org.kiwiproject.beta.base;

import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;

/**
 * Exception thrown when a value cannot be cast to the expected type.
 * <p>
 * This exists mainly to provide more information in certain situations
 * than {@link ClassCastException} does. For example, when checking that
 * a List contains only Integer values but a Double is found. Or when
 * checking that a Map contains the expected key and value types.
 * <p>
 * While you can construct an instance using the "standard" constructors,
 * it is preferable to create instances the factory methods which can
 * provide better semantic meaning, and better messages describing the
 * problem.
 */
@Beta
public class TypeMismatchException extends RuntimeException {

    /**
     * Constructs a new instance with no detail message.
     */
    public TypeMismatchException() {
    }

    /**
     * Constructs a new instance with the specified detail message.
     *
     * @param message the detail message
     */
    public TypeMismatchException(String message) {
        super(message);
    }

    /**
     * Constructs a new instance with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public TypeMismatchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance with the specified cause and a detail message
     * of {@code (cause==null ? null : cause.toString())}.
     *
     * @param cause the cause
     */
    public TypeMismatchException(Throwable cause) {
        super(cause);
    }

    /**
     * Factory method to create a new instance with a standardized message for a type mismatch.
     *
     * @param expectedType the expected type of the value
     * @param cause        the ClassCastException that occurred during the cast attempt
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forExpectedTypeWithCause(Class<?> expectedType, ClassCastException cause) {
        return new TypeMismatchException(f("Cannot cast value to type {}", expectedType.getName()), cause);
    }

    /**
     * Factory method to create a new instance with a standardized message for a type mismatch.
     *
     * @param expectedType the expected type of the value
     * @param actualType   the actual type of the value
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedType(Class<?> expectedType, Class<?> actualType) {
        return new TypeMismatchException(f("Cannot cast value of type {} to type {}",
                actualType.getName(), expectedType.getName()));
    }

     /**
     * Factory method to create a new instance with a standardized message for a type mismatch.
     *
     * @param expectedType the expected type of the value
     * @param actualType   the actual type of the value
     * @param cause        the ClassCastException that occurred during the cast attempt
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedTypeWithCause(Class<?> expectedType,
                                                                   Class<?> actualType,
                                                                   ClassCastException cause) {
        var message = f("Cannot cast value of type {} to type {}",
                actualType.getName(), expectedType.getName());
        return new TypeMismatchException(message, cause);
    }

    /**
     * Factory method to create a new instance with a standardized message for a type mismatch
     * on a collection element.
     * 
     * @param collectionType the type of collection, such as Collection, List, or Set
     * @param expectedType   the expected type of the collection elements
     * @param actualType     the actual type found in the collection
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedCollectionElementType(Class<?> collectionType,
                                                                           Class<?> expectedType,
                                                                           Class<?> actualType) {
        var message = f("Expected {} to contain elements of type {}, but found element of type {}",
                collectionType.getName(), expectedType.getName(), actualType.getName());
        return new TypeMismatchException(message);
    }

    /**
     * Factory method to create a new instance with a standardized message for a map key type mismatch.
     * 
     * @param expectedKeyType the expected type of keys in the map
     * @param actualKeyType   the actual type of key found in the map
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedMapKeyType(Class<?> expectedKeyType,
                                                                Class<?> actualKeyType) {
        var message = f("Expected Map to contain keys of type {}, but found key of type {}",
                expectedKeyType.getName(), actualKeyType.getName());
        return new TypeMismatchException(message);
    }

    /**
     * Factory method to create a new instance with a standardized message for a map value type mismatch.
     * 
     * @param expectedValueType the expected type of values in the map
     * @param actualValueType   the actual type of value found in the map
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedMapValueType(Class<?> expectedValueType,
                                                                  Class<?> actualValueType) {
        var message = f("Expected Map to contain values of type {}, but found value of type {}",
                expectedValueType.getName(), actualValueType.getName());
        return new TypeMismatchException(message);
    }
}
