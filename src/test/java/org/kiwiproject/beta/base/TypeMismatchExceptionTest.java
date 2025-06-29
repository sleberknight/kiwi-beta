package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

import java.util.Set;

class TypeMismatchExceptionTest {

    @Test
    void shouldCreate_forTypeMismatch_WithValueType_AndUnexpectedType() {
        var ex = TypeMismatchException.forUnexpectedType(Double.class, Integer.class);

        assertAll(
                () -> assertThat(ex).hasMessage("Cannot cast value of type java.lang.Integer to type java.lang.Double"),
                () -> assertThat(ex).hasNoCause(),
                () -> assertThat(ex.expectedType()).isEqualTo(Double.class),
                () -> assertThat(ex.actualType()).isEqualTo(Integer.class)
        );
    }

    @Test
    void shouldCreate_forTypeMismatch_WithValueType_AndUnexpectedType_AndClassCastExceptionAsCause() {
        var cause = new ClassCastException();
        var ex = TypeMismatchException.forUnexpectedTypeWithCause(String.class, Integer.class, cause);

        assertAll(
                () -> assertThat(ex).hasMessage("Cannot cast value of type java.lang.Integer to type java.lang.String"),
                () -> assertThat(ex).hasCause(cause),
                () -> assertThat(ex.expectedType()).isEqualTo(String.class),
                () -> assertThat(ex.actualType()).isEqualTo(Integer.class)
        );
    }

    @Test
    void shouldCreate_forUnexpectedCollectionElementType() {
        var ex = TypeMismatchException.forUnexpectedCollectionElementType(Set.class, Integer.class, Double.class);

        assertAll(
                () -> assertThat(ex).hasMessage("Expected java.util.Set to contain elements of type java.lang.Integer, but found element of type java.lang.Double"),
                () -> assertThat(ex).hasNoCause(),
                () -> assertThat(ex.expectedType()).isEqualTo(Integer.class),
                () -> assertThat(ex.actualType()).isEqualTo(Double.class)
        );
    }

    @Test
    void shouldCreate_forUnexpectedMapKeyType() {
        var ex = TypeMismatchException.forUnexpectedMapKeyType(String.class, Integer.class);

        assertAll(
                () -> assertThat(ex).hasMessage("Expected Map to contain keys of type java.lang.String, but found key of type java.lang.Integer"),
                () -> assertThat(ex).hasNoCause(),
                () -> assertThat(ex.expectedType()).isEqualTo(String.class),
                () -> assertThat(ex.actualType()).isEqualTo(Integer.class)
        );
    }

    @Test
    void shouldCreate_forUnexpectedMapValueType() {
        var ex = TypeMismatchException.forUnexpectedMapValueType(Integer.class, Long.class);

        assertAll(
                () -> assertThat(ex).hasMessage("Expected Map to contain values of type java.lang.Integer, but found value of type java.lang.Long"),
                () -> assertThat(ex).hasNoCause(),
                () -> assertThat(ex.expectedType()).isEqualTo(Integer.class),
                () -> assertThat(ex.actualType()).isEqualTo(Long.class)
        );
    }
}
