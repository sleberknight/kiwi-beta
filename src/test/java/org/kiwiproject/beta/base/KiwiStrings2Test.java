package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;

import java.util.stream.Stream;

@DisplayName("KiwiStrings2")
public class KiwiStrings2Test {

    @Nested
    class CamelToSnakeCase {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankInput(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiStrings2.camelToSnakeCase(value))
                    .withMessage("value must not be blank");
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.base.KiwiStrings2Test#camelCaseValues")
        void shouldConvertTo_SnakeCase(String input, String expected) {
            assertThat(KiwiStrings2.camelToSnakeCase(input)).isEqualTo(expected);
        }
    }

    @Nested
    class CamelToSnakeCaseOrEmpty {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldReturnEmpty_GivenBlankInput(String value) {
            assertThat(KiwiStrings2.camelToSnakeCaseOrEmpty(value)).isEmpty();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.base.KiwiStrings2Test#camelCaseValues")
        void shouldConvertTo_SnakeCase(String input, String expected) {
            assertThat(KiwiStrings2.camelToSnakeCaseOrEmpty(input)).contains(expected);
        }
    }

    @Nested
    class CamelToSnakeCaseOrNull {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldReturnNull_GivenBlankInput(String value) {
            assertThat(KiwiStrings2.camelToSnakeCaseOrNull(value)).isNull();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.base.KiwiStrings2Test#camelCaseValues")
        void shouldConvertTo_SnakeCase(String input, String expected) {
            assertThat(KiwiStrings2.camelToSnakeCaseOrNull(input)).isEqualTo(expected);
        }
    }

    static Stream<Arguments> camelCaseValues() {
        return Stream.of(
                Arguments.of("id", "id"),
                Arguments.of("source", "source"),
                Arguments.of("tagsJson", "tags_json"),
                Arguments.of("createdAt", "created_at"),
                Arguments.of("createdById", "created_by_id"),
                Arguments.of("expirationDate", "expiration_date"),
                Arguments.of("MarkedForRemoval", "marked_for_removal"),
                Arguments.of("DeletedAt", "deleted_at"),
                Arguments.of("created_at", "created_at"),
                Arguments.of("updated_at", "updated_at"));
    }

    @Nested
    class ReplaceNullCharactersWithEmpty {

        @Test
        void shouldReturnNullString_WhenGivenNull() {
            assertThat(KiwiStrings2.replaceNullCharactersWithEmpty(null)).isNull();
        }

        @Test
        void shouldReplaceNullCharacters() {
            var str = "this string \u0000 contains several \u0000 characters in \u0000 it";
            assertThat(KiwiStrings2.replaceNullCharactersWithEmpty(str))
                    .isEqualTo("this string  contains several  characters in  it");
        }

        @Test
        void shouldReturnSameString_WhenDoesNotContainNullCharacters() {
            var str = "this string does NOT contain any null characters in it";
            assertThat(KiwiStrings2.replaceNullCharactersWithEmpty(str))
                    .isSameAs(str);
        }
    }

    @Nested
    class ReplaceNullCharacters {

        @Test
        void shouldReturnDefaultValue_WhenGivenNull() {
            assertThat(KiwiStrings2.replaceNullCharacters(null, "", "42"))
                    .isEqualTo("42");
        }

        @Test
        void shouldReplaceNullCharacters() {
            var str = "this string \u0000 contains several \u0000 characters in \u0000 it";
            assertThat(KiwiStrings2.replaceNullCharacters(str, "NULL", ""))
                    .isEqualTo("this string NULL contains several NULL characters in NULL it");
        }

        @Test
        void shouldReturnSameString_WhenDoesNotContainNullCharacters() {
            var str = "this string does NOT contain any null characters in it";
            assertThat(KiwiStrings2.replaceNullCharacters(str, "", null))
                    .isSameAs(str);
        }
    }
}
