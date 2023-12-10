package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

@DisplayName("KiwiEnums2")
class KiwiEnums2Test {

    enum Season {
        FALL, WINTER, SPRING, SUMMER
    }

    enum Color {
        RED, GREEN, BLUE
    }

    @Nested
    class Entries {

        @Test
        void shouldReturnEnumConstantsAsList() {
            assertAll(
                    () -> assertThat(KiwiEnums2.entries(Season.class))
                            .containsExactly(Season.FALL, Season.WINTER, Season.SPRING, Season.SUMMER),
                    () -> assertThat(KiwiEnums2.entries(Color.class))
                            .containsExactly(Color.RED, Color.GREEN, Color.BLUE)
            );
        }

        @Test
        void shouldReturnImmutableList() {
            assertThat(KiwiEnums2.entries(Season.class)).isUnmodifiable();
        }

        /**
         * @implNote This declares the argument type to be a class E that extends Enum, which works due
         * to type erasure. None of the arguments are classes that extend Enum, since the point of
         * this test is to ensure we don't allow classes that aren't Enums.
         */
        @ParameterizedTest
        @ValueSource(classes = {Object.class, String.class, Map.class, List.class})
        <E extends Enum<E>> void shouldRequireEnumClasses(Class<E> clazz) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiEnums2.entries(clazz))
                    .withMessage("%s is not an enum", clazz);
        }
    }
}
