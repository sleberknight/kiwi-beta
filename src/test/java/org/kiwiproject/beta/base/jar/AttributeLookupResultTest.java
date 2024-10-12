package org.kiwiproject.beta.base.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("AttributeLookupResult")
class AttributeLookupResultTest {

    @ParameterizedTest
    @EnumSource(value = AttributeLookupStatus.class, names = "EXISTS", mode = EnumSource.Mode.EXCLUDE)
    void shouldRequireNullValue_WhenLookupFails(AttributeLookupStatus lookupStatus) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new AttributeLookupResult(lookupStatus, "a value", null))
                .withMessage("value must be null when lookup fails");
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            EXISTS, some-value, true
            DOES_NOT_EXIST, null, false
            FAILURE, null, false
            """, nullValues = "null")
    void shouldHaveConsistentSucceededAndFailed(AttributeLookupStatus lookupStatus,
                                                String value,
                                                boolean expectedSuccessValue) {

        var result = new AttributeLookupResult(lookupStatus, value, null);

        assertAll(
                () -> assertThat(result.succeeded()).isEqualTo(expectedSuccessValue),
                () -> assertThat(result.failed()).isNotEqualTo(result.succeeded()),
                () -> assertThat(result.containsValue()).isEqualTo(expectedSuccessValue),
                () -> assertThat(result.maybeValue().isPresent()).isEqualTo(expectedSuccessValue),
                () -> assertThat(result.error()).isNull()
        );
    }

    @Nested
    class ValueOrThrow {

        @Test
        void shouldReturnValue_WhenIsNotNull() {
            var result = new AttributeLookupResult(AttributeLookupStatus.EXISTS, "42", null);

            assertThat(result.valueOrThrow()).isEqualTo("42");
        }

        @Test
        void shouldThrowIllegalStateException_WhenValueIsNull() {
            var result = new AttributeLookupResult(AttributeLookupStatus.DOES_NOT_EXIST, null, null);

            assertThatIllegalStateException()
                    .isThrownBy(result::valueOrThrow)
                    .withMessage("expected value not to be null");
        }
    }
}
