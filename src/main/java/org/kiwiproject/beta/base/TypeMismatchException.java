package org.kiwiproject.beta.base;

import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;

/**
 * Exception thrown when a value cannot be cast to the expected type.
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
     * @param valueType the expected type of the value
     * @param cause     the ClassCastException that occurred during the cast attempt
     * @return a new instance with a descriptive message
     */
    public static TypeMismatchException forTypeMismatch(Class<?> valueType, ClassCastException cause) {
        return new TypeMismatchException(f("Cannot cast value to type {}", valueType.getName()), cause);
    }

    public static TypeMismatchException forCollectionTypeMismatch(Class<?> collectionType,
                                                                  Class<?> valueType,
                                                                  Class<?> unexpectedType) {
        var message = f("Expected {} to contain elements of type {}, but found element of type {}",
                collectionType.getName(), valueType.getName(), unexpectedType.getName());
        return new TypeMismatchException(message);
    }

    public static TypeMismatchException forMapKeyTypeMismatch(Class<?> keyType,
                                                              Class<?> unexpectedKeyType) {
        var message = f("Expected Map to contain keys of type {}, but found key of type {}",
                keyType.getName(), unexpectedKeyType.getName());
        return new TypeMismatchException(message);
    }

    public static TypeMismatchException forMapValueTypeMismatch(Class<?> valueType,
                                                                Class<?> unexpectedValueType) {
        var message = f("Expected Map to contain values of type {}, but found value of type {}",
                valueType.getName(), unexpectedValueType.getName());
        return new TypeMismatchException(message);
    }
}
