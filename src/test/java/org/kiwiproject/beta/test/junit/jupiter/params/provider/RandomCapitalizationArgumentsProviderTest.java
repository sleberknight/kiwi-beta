package org.kiwiproject.beta.test.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.beta.test.junit.jupiter.params.provider.RandomCapitalizationSource.Type;
import org.kiwiproject.test.junit.jupiter.params.provider.MinimalBlankStringSource;
import org.kiwiproject.test.junit.jupiter.params.provider.RandomIntSource;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

@DisplayName("RandomCapitalizationArgumentsProvider")
class RandomCapitalizationArgumentsProviderTest {

    @ParameterizedTest
    @ValueSource(ints = { 3, 5, 10, 25 })
    void shouldProvideTheExpectedNumberOfRandomlyCapitalizedValues(int count) {
        var annotation = newRandomCapitalization("error", count, Type.RANDOM);
        var provider = newRandomCapitalizationArgumentsProvider(annotation);

        var arguments = getProviderArguments(provider);

        assertThat(arguments).hasSize(count);
    }

    @ParameterizedTest
    @ValueSource(strings = { "a", "ab", "abc", "abcd", "abcde" })
    void shouldProvideTheMaximumMathematicallyPossibleRandomCapitalizationVariants(String value) {
        var maxPossibleVariants = (int) Math.pow(2, value.length());
        var count = maxPossibleVariants + 1;
        var annotation = newRandomCapitalization(value, count, Type.RANDOM);
        var provider = newRandomCapitalizationArgumentsProvider(annotation);

        var arguments = getProviderArguments(provider);

        assertThat(arguments).hasSize(maxPossibleVariants);
    }

    @Test
    void shouldProvideTheThreeStandardCapitalizedValues_AndIgnoreCountIfSpecified() {
        var countLargerThanThree = ThreadLocalRandom.current().nextInt(4, 21);
        var annotation = newRandomCapitalization("success", countLargerThanThree, Type.STANDARD);
        var provider = newRandomCapitalizationArgumentsProvider(annotation);

        var arguments = getProviderArguments(provider);

        assertThat(arguments).containsExactlyInAnyOrder("SUCCESS", "success", "Success");
    }

    @ParameterizedTest
    @MinimalBlankStringSource
    void shouldRequireValue(String value) {
        var annotation = newRandomCapitalization(value, 3, Type.RANDOM);

        var provider = newRandomCapitalizationArgumentsProvider(annotation);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> provider.provideArguments(null, null))
                .withMessage("value must not be blank");
    }

    @ParameterizedTest
    @RandomIntSource(max = 0)
    void shouldRequireCountGreaterThanZero(int count) {
        var annotation = newRandomCapitalization("abc", count, Type.RANDOM);

        var provider = newRandomCapitalizationArgumentsProvider(annotation);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> provider.provideArguments(null, null))
                .withMessage("count must be greater than zero");
    }

    private static RandomCapitalizationArgumentsProvider newRandomCapitalizationArgumentsProvider(
            RandomCapitalizationSource annotation) {

        var provider = new RandomCapitalizationArgumentsProvider();
        provider.accept(annotation);
        return provider;
    }

    private static String[] getProviderArguments(RandomCapitalizationArgumentsProvider provider) {
        return provider.provideArguments(null, null)
                .map(Arguments::get)
                .flatMap(Arrays::stream)
                .map(String.class::cast)
                .toArray(String[]::new);
    }

    private static RandomCapitalizationSource newRandomCapitalization(String value, int count, Type type) {
        return new RandomCapitalizationSource() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return RandomCapitalizationSource.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public int count() {
                return count;
            }

            @Override
            public Type type() {
                return type;
            }
        };
    }

    @DisplayName("UsingTheAnnotation")
    @Nested
    class IntegrationTests {

        @ParameterizedTest
        @RandomCapitalizationSource("abc")
        void shouldProvideRandomCapitalization(String input) {
            assertThat(input).isEqualToIgnoringCase("abc");
        }

        @ParameterizedTest
        @RandomCapitalizationSource(value = "error", type = Type.STANDARD)
        void shouldProvideStandardCapitalization(String input) {
            assertThat(input).isIn("error", "ERROR", "Error");
        }

        @ParameterizedTest
        @RandomCapitalizationSource(value = "abc", count = 5)
        void shouldProvideRandomCapitalizationWithCount(String input) {
            assertThat(input).isEqualToIgnoringCase("abc");
        }
    }
}
