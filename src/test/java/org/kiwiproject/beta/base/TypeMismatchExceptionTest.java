package org.kiwiproject.beta.base;

import static org.kiwiproject.test.junit.jupiter.StandardExceptionTests.standardConstructorTestsFor;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;

class TypeMismatchExceptionTest {

    @TestFactory
    Collection<DynamicTest> shouldHaveStandardConstructors() {
        return standardConstructorTestsFor(TypeMismatchException.class);
    }
}
