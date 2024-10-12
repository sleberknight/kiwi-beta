package org.kiwiproject.beta.base.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

@DisplayName("AttributesLookupResult")
class AttributesLookupResultTest {

    @ParameterizedTest
    @EnumSource(value = AttributesLookupStatus.class, names = "SUCCESS", mode = EnumSource.Mode.EXCLUDE)
    void shouldRequireNullValue_WhenLookupFails(AttributesLookupStatus lookupStatus) {
        var attributes = Map.of(
                "Some-Attr", "Some-Value"
        );

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new AttributesLookupResult(lookupStatus, attributes, null))
                .withMessage("attributes must be null when lookup fails");
    }

    @Test
    void shouldHaveAttributesMap_ForSuccess() {
        var attributes = Map.of(
                "Attribute-1", "Value-1",
                "Attribute-2", "Value-2"
        );

        var result = new AttributesLookupResult(AttributesLookupStatus.SUCCESS, attributes, null);

        assertAll(
                () -> assertThat(result.succeeded()).isTrue(),
                () -> assertThat(result.failed()).isFalse(),
                () -> assertThat(result.hasAttributes()).isTrue(),
                () -> assertThat(result.attributes()).isEqualTo(attributes),
                () -> assertThat(result.maybeAttributes()).contains(attributes),
                () -> assertThat(result.error()).isNull()
        );
    }

    @Test
    void shouldNotHaveAttributesMap_ForFailure() {
        var result = new AttributesLookupResult(AttributesLookupStatus.FAILURE, null, null);

        assertAll(
                () -> assertThat(result.succeeded()).isFalse(),
                () -> assertThat(result.failed()).isTrue(),
                () -> assertThat(result.hasAttributes()).isFalse(),
                () -> assertThat(result.attributes()).isNull(),
                () -> assertThat(result.maybeAttributes()).isEmpty(),
                () -> assertThat(result.error()).isNull()
        );
    }

    @Test
    void canHaveError_ForFailure() {
        var error = new RuntimeException("some error");
        var result = new AttributesLookupResult(AttributesLookupStatus.FAILURE, null, error);

        assertAll(
                () -> assertThat(result.succeeded()).isFalse(),
                () -> assertThat(result.failed()).isTrue(),
                () -> assertThat(result.hasAttributes()).isFalse(),
                () -> assertThat(result.attributes()).isNull(),
                () -> assertThat(result.maybeAttributes()).isEmpty(),
                () -> assertThat(result.error()).isSameAs(error)
        );
    }
}
