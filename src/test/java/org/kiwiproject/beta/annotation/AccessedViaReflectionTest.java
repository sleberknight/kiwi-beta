package org.kiwiproject.beta.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("@AccessedViaReflection")
class AccessedViaReflectionTest {

    @Test
    void shouldNotRetain_OnConstructor() throws NoSuchMethodException {
        var ctor = ReflectedUpon.class.getDeclaredConstructor(String.class);
        assertThat(ctor.getAnnotations()).isEmpty();
    }

    @Test
    void shouldNotRetain_OnMethod() throws NoSuchMethodException {
        var method = ReflectedUpon.class.getDeclaredMethod("updateValue", String.class);
        assertThat(method.getAnnotations()).isEmpty();
    }

    static class ReflectedUpon {

        String value;

        @AccessedViaReflection
        ReflectedUpon(String value) {
            this.value = value;
        }

        @AccessedViaReflection("Accessed reflectively by AccessedViaReflectionTest")
        String updateValue(String newValue) {
            var oldValue = value;
            value = newValue;
            return oldValue;
        }
    }

}
