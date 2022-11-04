package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;

@DisplayName("KiwiStrings2")
public class KiwiStrings2Test {

    @Nested
    class CamelToSnakeCaseOrNull {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldReturnNull_GivenBlnkInput(String value) {
            assertThat(KiwiStrings2.camelToSnakeCaseOrNull(value)).isNull();
        }

        @ParameterizedTest
        @CsvSource({
            "id, id",
            "source, source",
            "tagsJson, tags_json",
            "createdAt, created_at",
            "createdById, created_by_id",
            "expirationDate, expiration_date",
            "MarkedForRemoval, marked_for_removal",
            "DeletedAt, deleted_at",
            "created_at, created_at",
            "updated_at, updated_at",
        })
        void shouldConvertTo_SnakeCase(String input, String expected) {
            assertThat(KiwiStrings2.camelToSnakeCaseOrNull(input)).isEqualTo(expected);
        }
    }
}
