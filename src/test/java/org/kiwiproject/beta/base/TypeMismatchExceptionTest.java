package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.kiwiproject.test.junit.jupiter.StandardExceptionTests.standardConstructorTestsFor;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;
import java.util.Set;

class TypeMismatchExceptionTest {

    @TestFactory
    Collection<DynamicTest> shouldHaveStandardConstructors() {
        return standardConstructorTestsFor(TypeMismatchException.class);
    }

    @Test
    void shouldCreate_forTypeMismatch_AndClassCastExceptionAsCause() {
        var cause = new ClassCastException();
        var ex = TypeMismatchException.forTypeMismatch(Double.class, cause);

        assertAll(
            () -> assertThat(ex).hasMessage("Cannot cast value to type java.lang.Double"),
            () -> assertThat(ex).hasCause(cause)
        );
    }

    @Test
    void shouldCreate_forTypeMismatch_WithValueType_AndUnexpectedType() {
        var ex = TypeMismatchException.forTypeMismatch(Double.class, Integer.class);

         assertAll(
            () -> assertThat(ex).hasMessage("Cannot cast value of type java.lang.Integer to type java.lang.Double"),
            () -> assertThat(ex).hasNoCause()
        );
    }

    @Test
    void shouldCreate_forTypeMismatch_WithValueType_AndUnexpectedTypeh_AndClassCastExceptionAsCause() {
        var cause = new ClassCastException();
        var ex = TypeMismatchException.forTypeMismatch(Double.class, Integer.class, cause);

         assertAll(
            () -> assertThat(ex).hasMessage("Cannot cast value of type java.lang.Integer to type java.lang.Double"),
            () -> assertThat(ex).hasCause(cause)
        );
    }

    @Test
    void shouldCreate_forCollectionTypeMismatch() {
        var ex = TypeMismatchException.forCollectionTypeMismatch(Set.class, Integer.class, Double.class);

        assertAll(
            () -> assertThat(ex).hasMessage("Expected java.util.Set to contain elements of type java.lang.Integer, but found element of type java.lang.Double"),
            () -> assertThat(ex).hasNoCause()
        );
    }

    @Test
    void shouldCreate_forMapKeyTypeMismatch() {
        var ex = TypeMismatchException.forMapKeyTypeMismatch(String.class, Integer.class);

        assertAll(
            () -> assertThat(ex).hasMessage("Expected Map to contain keys of type java.lang.String, but found key of type java.lang.Integer"),
            () -> assertThat(ex).hasNoCause()
        );
    }

    @Test
    void shouldCreate_forMapValueTypeMismatch() {
        var ex = TypeMismatchException.forMapValueTypeMismatch(Integer.class, Long.class);

        assertAll(
            () -> assertThat(ex).hasMessage("Expected Map to contain values of type java.lang.Integer, but found value of type java.lang.Long"),
            () -> assertThat(ex).hasNoCause()
        );
    }
}
