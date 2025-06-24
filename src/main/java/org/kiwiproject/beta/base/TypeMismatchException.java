package org.kiwiproject.beta.base;

import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;

/**
 * Exception thrown when a value cannot be cast to the expected type.
 * <p>
 * This exists mainly to provide more information in certain situations
 * than {@link ClassCastException} does. For example, when checking that
 * a List contains only Integer values, but a Double is found. Or when
 * checking that a Map contains the expected key and value types.
 */
@Beta
public class TypeMismatchException extends RuntimeException {

    private final Class<?> expectedType;
    private final Class<?> actualType;

    private TypeMismatchException(String message,
                                  Class<?> expectedType,
                                  Class<?> actualType,
                                  Throwable cause) {
        super(message, cause);
        this.expectedType = requireNotNull(expectedType, "expectedType must not be null");
        this.actualType = requireNotNull(actualType, "actualType must not be null");
    }

    /**
     * Gets the expected type of the value that caused the type mismatch.
     *
     * @return the Class object representing the expected type
     */
    public Class<?> expectedType() {
        return expectedType;
    }

    /**
     * Gets the actual type of the value that caused the type mismatch.
     *
     * @return the Class object representing the actual type
     */
    public Class<?> actualType() {
        return actualType;
    }

    /**
     * Factory method to create a new instance with a standardized message for a type mismatch and no cause.
     *
     * @param expectedType the expected type of the value
     * @param actualType   the actual type of the value
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedType(Class<?> expectedType, Class<?> actualType) {
        var message = f("Cannot cast value of type {} to type {}", actualType.getName(), expectedType.getName());
        return TypeMismatchException.forUnexpectedTypeWithMessage(message, expectedType, actualType, null);
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
        var message = f("Cannot cast value of type {} to type {}", actualType.getName(), expectedType.getName());
        return TypeMismatchException.forUnexpectedTypeWithMessage(message, expectedType, actualType, cause);
    }

    /**
     * Factory method to create a new instance with a standardized message for a type mismatch
     * on a collection element. The returned exception has no cause.
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
        return TypeMismatchException.forUnexpectedTypeWithMessage(message, expectedType, actualType, null);
    }

    /**
     * Factory method to create a new instance with a standardized message for a map key type mismatch.
     * The returned exception has no cause.
     * 
     * @param expectedKeyType the expected type of keys in the map
     * @param actualKeyType   the actual type of key found in the map
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedMapKeyType(Class<?> expectedKeyType,
                                                                Class<?> actualKeyType) {
        var message = f("Expected Map to contain keys of type {}, but found key of type {}",
                expectedKeyType.getName(), actualKeyType.getName());
        return TypeMismatchException.forUnexpectedTypeWithMessage(message, expectedKeyType, actualKeyType, null);
    }

    /**
     * Factory method to create a new instance with a standardized message for a map value type mismatch.
     * The returned exception has no cause.
     * 
     * @param expectedValueType the expected type of values in the map
     * @param actualValueType   the actual type of value found in the map
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forUnexpectedMapValueType(Class<?> expectedValueType,
                                                                  Class<?> actualValueType) {
        var message = f("Expected Map to contain values of type {}, but found value of type {}",
                expectedValueType.getName(), actualValueType.getName());
        return TypeMismatchException.forUnexpectedTypeWithMessage(message, expectedValueType, actualValueType, null);
    }

    /**
     * Factory method to create a new instance of {@code TypeMismatchException} with a custom message,
     * expected type, actual type, and an optional cause.
     *
     * @param message      a custom descriptive message explaining the type mismatch
     * @param expectedType the expected type of the value
     * @param actualType   the actual type of the value
     * @param cause        the (optional) {@code ClassCastException} that occurred during the cast attempt
     * @return a new instance of {@code TypeMismatchException} representing the type mismatch
     */
    public static TypeMismatchException forUnexpectedTypeWithMessage(String message,
                                                                     Class<?> expectedType,
                                                                     Class<?> actualType,
                                                                     ClassCastException cause) {
        return new TypeMismatchException(message, expectedType, actualType, cause);
    }
}
