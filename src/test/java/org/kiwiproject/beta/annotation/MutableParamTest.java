package org.kiwiproject.beta.annotation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.collect.KiwiArrays.first;

import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This "test" is really just to validate usage of the annotation, though it
 * does verify that the annotation is not available at runtime.
 */
@DisplayName("@MutableParam")
class MutableParamTest {

    @Test
    void shouldNotRetainAtRuntime_OnConstructorParam() {
        var t = new Testing(new ArrayList<>());
        var ctor = first(t.getClass().getDeclaredConstructors());
        var param = first(ctor.getParameters());
        assertThat(param.getAnnotations()).isEmpty();
    }

    @Test
    void shouldNotRetainAtRuntime_OnMethodParam() throws NoSuchMethodException {
        var method = Testing.class.getDeclaredMethod("put", Map.class, Object.class, Object.class);
        assertThat(method).isNotNull();
        var firstParam = first(method.getParameters());
        assertThat(firstParam.getType()).isEqualTo(Map.class);  // smoke test
        assertThat(firstParam.getAnnotations()).isEmpty();
    }

    /**
     * This class is NOT an example of good design...
     */
    @SuppressWarnings("ClassCanBeRecord")
    static class Testing {

        @Getter
        private final List<String> strings;

        Testing(@MutableParam("adds elements to the list") List<String> strings) {
            strings.add("foo");
            strings.add("bar");
            strings.add("baz");

            this.strings = strings;
        }

        static <K, V> void put(@MutableParam Map<K, V> map, K key, V value) {
            map.put(key, value);
        }
    }
}
