package org.kiwiproject.beta.dao;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@DisplayName("AllowedFields")
@ExtendWith(SoftAssertionsExtension.class)
class AllowedFieldsTest {

    @Nested
    class FactoryMethods {

        @Test
        void shouldHandleSimpleFieldNames(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "lastName", "email", "zip");

            softly.assertThat(allowedFields.getPrefixedFieldName("firstName")).isEqualTo("firstName");
            softly.assertThat(allowedFields.getPrefixedFieldName("lastName")).isEqualTo("lastName");
            softly.assertThat(allowedFields.getPrefixedFieldName("email")).isEqualTo("email");
            softly.assertThat(allowedFields.getPrefixedFieldName("zip")).isEqualTo("zip");
        }

        @Test
        void shouldHandleSimplePrefixedFieldNames(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("user.firstName", "user.lastName", "user.email", "user.zip");

            softly.assertThat(allowedFields.getPrefixedFieldName("firstName")).isEqualTo("user.firstName");
            softly.assertThat(allowedFields.getPrefixedFieldName("lastName")).isEqualTo("user.lastName");
            softly.assertThat(allowedFields.getPrefixedFieldName("email")).isEqualTo("user.email");
            softly.assertThat(allowedFields.getPrefixedFieldName("zip")).isEqualTo("user.zip");
        }

        @Test
        void shouldHandleCompoundPrefixedFieldNames(SoftAssertions softly) {
            var allowedFields = AllowedFields.of(
                    "account.user.firstName", "account.user.lastName", "account.user.email", "account.user.zip");

            softly.assertThat(allowedFields.getPrefixedFieldName("firstName")).isEqualTo("account.user.firstName");
            softly.assertThat(allowedFields.getPrefixedFieldName("lastName")).isEqualTo("account.user.lastName");
            softly.assertThat(allowedFields.getPrefixedFieldName("email")).isEqualTo("account.user.email");
            softly.assertThat(allowedFields.getPrefixedFieldName("zip")).isEqualTo("account.user.zip");
        }

        @Test
        void shouldHandleMixedSimpleAndPrefixedFieldNames(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThat(allowedFields.getPrefixedFieldName("firstName")).isEqualTo("firstName");
            softly.assertThat(allowedFields.getPrefixedFieldName("lastName")).isEqualTo("user.lastName");
            softly.assertThat(allowedFields.getPrefixedFieldName("email")).isEqualTo("account.user.email");
        }

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankFieldNamesInVarargs(String blankName) {
            assertThatThrownBy(() -> AllowedFields.of("age", "zip", blankName))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("field name must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = { ".", ".firstName", "user.lastName.", ".account.user.email.", "account.user.email." })
        void shouldNotAllowIllegalFieldNamesInVarargs(String illegalName) {
            assertThatThrownBy(() -> AllowedFields.of("age", "zip", illegalName))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "field name must be simple (firstName) or compound (user.firstName, account.user.firstName; cannot being or end with '.' (dot)");
        }

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankFieldNamesInCollection(String blankName) {
            var fieldNames = new HashSet<>(Arrays.asList("age", "zip", blankName));
            assertThatThrownBy(() -> AllowedFields.of(fieldNames))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("field name must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = { ".", ".firstName", "user.lastName.", ".account.user.email.", "account.user.email." })
        void shouldNotAllowIllegalFieldNamesInCollection(String illegalName) {
            var fieldNames = new HashSet<>(Arrays.asList("age", "zip", illegalName));
            assertThatThrownBy(() -> AllowedFields.of(fieldNames))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage(
                            "field name must be simple (firstName) or compound (user.firstName, account.user.firstName; cannot being or end with '.' (dot)");
        }

        @ParameterizedTest
        @NullSource
        void shouldNotAllowNullFieldNamesVararg(String[] fieldNames) {
            assertThatThrownBy(() -> AllowedFields.of(fieldNames))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("fieldNames must not be null");
        }
        
        @ParameterizedTest
        @EmptySource
        void shouldNotAllowEmptyFieldNamesVararg(String[] fieldNames) {
            assertThatThrownBy(() -> AllowedFields.of(fieldNames))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("at least one field name must be specified");
        }

        @ParameterizedTest
        @NullSource
        void shouldNotAllowNullFieldNamesList(List<String> fieldNames) {
            assertThatThrownBy(() -> AllowedFields.of(fieldNames))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("fieldNames must not be null");
        }

        @ParameterizedTest
        @EmptySource
        void shouldNotAllowEmptyFieldNamesList(List<String> fieldNames) {
            assertThatThrownBy(() -> AllowedFields.of(fieldNames))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("at least one field name must be specified");
        }
    }

    @Nested
    class IsAllowed {

        @Test
        void shouldReturnTrueWhenAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThat(allowedFields.isAllowed("firstName")).isTrue();
            softly.assertThat(allowedFields.isAllowed("lastName")).isTrue();
            softly.assertThat(allowedFields.isAllowed("email")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenNotAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThat(allowedFields.isAllowed("middleName")).isFalse();
            softly.assertThat(allowedFields.isAllowed("zip")).isFalse();
            softly.assertThat(allowedFields.isAllowed("address.zip")).isFalse();
        }
    }

    @Nested
    class AssertAllowed {

        @Test
        void shouldNotThrowWhenAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThatCode(() -> allowedFields.assertAllowed("firstName")).doesNotThrowAnyException();
            softly.assertThatCode(() -> allowedFields.assertAllowed("lastName")).doesNotThrowAnyException();
            softly.assertThatCode(() -> allowedFields.assertAllowed("email")).doesNotThrowAnyException();
        }

        @Test
        void shouldThrowWhenNotAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThatThrownBy(() -> allowedFields.assertAllowed("middleName"))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("middleName is not allowed");

            softly.assertThatThrownBy(() -> allowedFields.assertAllowed("user.lastName"))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("user.lastName is not allowed");

            softly.assertThatThrownBy(() -> allowedFields.assertAllowed("account.user.email"))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("account.user.email is not allowed");
        }
    }

    @Nested
    class IsPrefixedAllowed {

        @Test
        void shouldReturnTrueWhenAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThat(allowedFields.isPrefixedAllowed("firstName")).isTrue();
            softly.assertThat(allowedFields.isPrefixedAllowed("user.lastName")).isTrue();
            softly.assertThat(allowedFields.isPrefixedAllowed("account.user.email")).isTrue();
        }

        @Test
        void shouldReturnFalseWhenNotAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThat(allowedFields.isPrefixedAllowed("middleName")).isFalse();
            softly.assertThat(allowedFields.isPrefixedAllowed("zip")).isFalse();
            softly.assertThat(allowedFields.isPrefixedAllowed("address.zip")).isFalse();
        }
    }

    @Nested
    class AssertPrefixedAllowed {

        @Test
        void shouldNotThrowWhenAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThatCode(() -> allowedFields.assertPrefixedAllowed("firstName")).doesNotThrowAnyException();
            softly.assertThatCode(() -> allowedFields.assertPrefixedAllowed("user.lastName"))
                    .doesNotThrowAnyException();
            softly.assertThatCode(() -> allowedFields.assertPrefixedAllowed("account.user.email"))
                    .doesNotThrowAnyException();
        }

        @Test
        void shouldThrowWhenNotAllowed(SoftAssertions softly) {
            var allowedFields = AllowedFields.of("firstName", "user.lastName", "account.user.email");

            softly.assertThatThrownBy(() -> allowedFields.assertPrefixedAllowed("middleName"))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("middleName is not allowed");

            softly.assertThatThrownBy(() -> allowedFields.assertPrefixedAllowed("user.zip"))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("user.zip is not allowed");

            softly.assertThatThrownBy(() -> allowedFields.assertPrefixedAllowed("account.user.address"))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("account.user.address is not allowed");
        }
    }
}
