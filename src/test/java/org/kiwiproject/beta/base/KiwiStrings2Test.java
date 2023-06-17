package org.kiwiproject.beta.base;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

@DisplayName("KiwiStrings2")
class KiwiStrings2Test {

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

    @Nested
    class RandomCaseVariantsWithoutSize {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldThrowIllegalArgumentException_GivenBlankInput(String input) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiStrings2.randomCaseVariants(input));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowIllegalArgumentException_GivenNullLocale() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiStrings2.randomCaseVariants("abc", null));
        }

        @RepeatedTest(5)
        void shouldGenerateThreeRandomCaseVariants() {
            var input = "abc";
            var strings = KiwiStrings2.randomCaseVariants(input);
            assertThat(strings).hasSize(3).doesNotHaveDuplicates();

            var uniqueLowerCaseStrings = strings.stream().map(String::toLowerCase).collect(toSet());
            assertThat(uniqueLowerCaseStrings).containsExactly(input);
        }

        @Test
        void shouldGenerateUnmodifiableSet() {
            assertThat(KiwiStrings2.randomCaseVariants("def")).isUnmodifiable();
        }

        @Test
        void shouldGenerateTwoCaseVariantsWhenInputStringHasOnlyOneCharacter() {
            var input = "z";
            var strings = KiwiStrings2.randomCaseVariants(input);
            assertThat(strings).containsExactlyInAnyOrder("z", "Z");
        }
    }

    @Nested
    class RandomCaseVariantsWithSize {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldThrowIllegalArgumentException_GivenBlankInput(String input) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiStrings2.randomCaseVariants(input, 5));
        }

        @ParameterizedTest
        @ValueSource(ints = { -10, -5, -1, 0 })
        void shouldThrowIllegalStateException_GivenNegativeOrZeroDesiredSize(int desiredSize) {
            assertThatIllegalStateException()
                    .isThrownBy(() -> KiwiStrings2.randomCaseVariants("abc", desiredSize));
        }

        @Test
        @SuppressWarnings("DataFlowIssue")
        void shouldThrowIllegalArgumentException_GivenNullLocale() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiStrings2.randomCaseVariants("abc", 5, null));
        }

        @RepeatedTest(5)
        void shouldGenerateDesiredNumberOfRandomCaseVariants() {
            var input = "abcde";
            var desiredSize = ThreadLocalRandom.current().nextInt(5, 11);
            var strings = KiwiStrings2.randomCaseVariants(input, desiredSize);
            assertThat(strings).hasSize(desiredSize).doesNotHaveDuplicates();

            var uniqueLowerCaseStrings = strings.stream().map(String::toLowerCase).collect(toSet());
            assertThat(uniqueLowerCaseStrings).containsExactly(input);
        }

        @Test
        void shouldGenerateUnmodifiableSet() {
            assertThat(KiwiStrings2.randomCaseVariants("def", 4)).isUnmodifiable();
        }

        @ParameterizedTest
        @CsvSource({
                "abc, 10",
                "abcd, 20",
                "abcdefg, 200",
                "abcdefghij, 1500",
        })
        void shouldLimitTheNumberOfVariants_WhenDesiredSizeIsMoreThanTheMaximumPossible(String input, int desiredSize) {
            var strings = KiwiStrings2.randomCaseVariants(input, desiredSize);
            var maximumSize = maxVariants(input);
            assertThat(desiredSize)
                    .describedAs("test precondition failure: desired size should be greater than maximum size")
                    .isGreaterThan(maximumSize);

            assertThat(strings).hasSize(maximumSize).doesNotHaveDuplicates();

            var uniqueLowerCaseStrings = strings.stream().map(String::toLowerCase).collect(toSet());
            assertThat(uniqueLowerCaseStrings).containsExactly(input);
        }

        @Test
        void shouldLimitTheMaximumNumberOfGeneratedVariantsWhenAttemptingToReachTheDesiredSize() {
            var input = "abcdefghijklmnopqrstuvwxyz";
            var desiredSize = maxVariants(input);
            var strings = KiwiStrings2.randomCaseVariants(input, desiredSize);

            assertThat(KiwiStrings2.randomCaseGenerationLimit())
                    .isEqualTo(KiwiStrings2.DEFAULT_RANDOM_CASE_GENERATION_LIMIT);
            assertThat(strings).hasSize(KiwiStrings2.DEFAULT_RANDOM_CASE_GENERATION_LIMIT).doesNotHaveDuplicates();
        }

        private int maxVariants(String input) {
            return (int) Math.pow(2, input.length());
        }

        @Test
        void shouldAllowChangingTheMaximumNumberOfGeneratedVariants() {
            assertThat(KiwiStrings2.randomCaseGenerationLimit)
                    .isEqualTo(KiwiStrings2.DEFAULT_RANDOM_CASE_GENERATION_LIMIT);

            try {
                var newLimit = 500;
                KiwiStrings2.setRandomCaseGenerationLimit(newLimit);
                assertThat(KiwiStrings2.randomCaseGenerationLimit()).isEqualTo(newLimit);

                var input = "abcdefghijklmnopqrstuvwxyz";
                var desiredSize = 1_000;
                var strings = KiwiStrings2.randomCaseVariants(input, desiredSize);

                assertThat(strings).hasSize(newLimit).doesNotHaveDuplicates();
            } finally {
                // Make sure we reset the limit!
                KiwiStrings2.resetRandomCaseGenerationLimit();
                assertThat(KiwiStrings2.randomCaseGenerationLimit)
                        .isEqualTo(KiwiStrings2.DEFAULT_RANDOM_CASE_GENERATION_LIMIT);
            }
        }

        @Test
        void shouldGenerateWhenInputLengthIsLongerThan32Characters() {
            var input = "The quick brown fox jumps over the lazy dog";
            var desiredSize = 25;
            var strings = KiwiStrings2.randomCaseVariants(input, desiredSize);
            assertThat(strings).hasSize(desiredSize).doesNotHaveDuplicates();

            var uniqueLowerCaseStrings = strings.stream().map(String::toLowerCase).collect(toSet());
            assertThat(uniqueLowerCaseStrings).containsExactly(input.toLowerCase());
        }
    }

    @Nested
    class StandardCaseVariants {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldThrowIllegalArgumentException_GivenBlankInput(String input) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiStrings2.standardCaseVariants(input));
        }

        @RepeatedTest(5)
        void shouldGenerateThreeStandardCaseVariants() {
            var strings = KiwiStrings2.standardCaseVariants("abc");

            assertThat(strings).containsExactlyInAnyOrder("ABC", "abc", "Abc");
        }

        @Test
        void shouldGenerateThreeStandardCaseVariants_GivenTwoCharacterLongString() {
            var strings = KiwiStrings2.standardCaseVariants("ab");
            assertThat(strings).containsExactlyInAnyOrder("AB", "ab", "Ab");
        }

        @Test
        void shouldGenerateOnlyUpperAndLowerCaseVariants_GivenOneCharacterLongString() {
            var strings = KiwiStrings2.standardCaseVariants("a");
            assertThat(strings).containsExactlyInAnyOrder("A", "a");
        }

        @Test
        void shouldGenerateUnmodifiableSet() {
            assertThat(KiwiStrings2.standardCaseVariants("def")).isUnmodifiable();
        }
    }
}
