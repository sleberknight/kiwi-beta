package org.kiwiproject.beta.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;

@DisplayName("KiwiMediaTypes")
class KiwiMediaTypesTest {

    @Nested
    class IsXml {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiMediaTypes.isXml(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/xml",
            "application/xml; charset=utf-8",
            "text/xml",
            "text/xml; charset=utf-8",
            "text/xml; charset=ISO-8859-1"
        })
        void shouldBeTrue_WhenGivenAnAcceptableXmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isXml(mediaType)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/json",
            "text/plain",
            "application/octet-stream",
            "application/x-www-form-urlencoded",
            "text/html",
            "image/png"
        })
        void shouldBeFalse_WhenNotAnXmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isXml(mediaType)).isFalse();
        }
    }

    @Nested
    class IsXmlWithJakartaMediaType {

        @Test
        void shouldNotAllowNullMediaType() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiMediaTypes.isXml((MediaType) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/xml",
            "application/xml; charset=utf-8",
            "text/xml",
            "text/xml; charset=utf-8",
            "text/xml; charset=ISO-8859-1"
        })
        void shouldBeTrue_WhenGivenAnAcceptableXmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isXml(MediaType.valueOf(mediaType))).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/json",
            "text/plain",
            "application/octet-stream",
            "application/x-www-form-urlencoded",
            "text/html",
            "image/png"
        })
        void shouldBeFalse_WhenNotAnXmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isXml(MediaType.valueOf(mediaType))).isFalse();
        }
    }

    @Nested
    class IsJson {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiMediaTypes.isJson(value));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/json",
            "application/json; charset=utf-8",
            "application/json; charset=ISO-8859-1",
        })
        void shouldBeTrue_WhenGivenAnAcceptableJsonType(String mediaType) {
            assertThat(KiwiMediaTypes.isJson(mediaType)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/xml",
            "text/css",
            "text/html; charset=utf-8",
            "text/xml; charset=ISO-8859-1",
            "image/jpeg"
        })
        void shouldBeFalse_WhenNotJsonType(String mediaType) {
            assertThat(KiwiMediaTypes.isJson(mediaType)).isFalse();
        }
    }

    @Nested
    class IsJsonWithJakartaMediaType {

        @Test
        void shouldNotAllowNullMediaType() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiMediaTypes.isJson((MediaType) null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/json",
            "application/json; charset=utf-8",
            "application/json; charset=ISO-8859-1",
        })
        void shouldBeTrue_WhenGivenAnAcceptableJsonType(String mediaType) {
            assertThat(KiwiMediaTypes.isJson(MediaType.valueOf(mediaType))).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "application/xml",
                "text/css",
                "text/html; charset=utf-8",
                "text/xml; charset=ISO-8859-1",
                "image/jpeg"
        })
        void shouldBeFalse_WhenNotJsonType(String mediaType) {
            assertThat(KiwiMediaTypes.isJson(MediaType.valueOf(mediaType))).isFalse();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "text/xml",
            "text/xml; charset=utf-8",
            "application/xml",
            "application/xml; charset=utf-8",
            "application/json",
            "application/json; charset=utf-8",
            "text/html; charset=utf-8",
            "text/html; charset=ISO-8859-1",
    })
    void shouldReturnJakartaMediaTypeAsStringWithoutParameters(String mediaType) {
        var jakartaMediaType = MediaType.valueOf(mediaType);
        var plainMediaType = KiwiMediaTypes.toStringWithoutParameters(jakartaMediaType);

        var expectedMediaType = com.google.common.net.MediaType.parse(mediaType).withoutParameters().toString();
        assertThat(plainMediaType).isEqualTo(expectedMediaType);
    }

    @Nested
    class MatchesType {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiMediaTypes.matchesType(value, "text"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "text/html",
            "text/plain",
            "text/css",
            "text/xml; charset=utf-8"
        })
        void shouldBeTrue_WhenTypesMatch(String mediaType) {
            assertThat(KiwiMediaTypes.matchesType(mediaType, "text")).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/json",
            "application/json; charset=utf-8",
            "image/png",
            "application/octet-stream",
        })
        void shouldBeFalse_WhenTypesDoNotMatch(String mediaType) {
            assertThat(KiwiMediaTypes.matchesType(mediaType, "text")).isFalse();
        }
    }

    @Nested
    class MatchesSubtype {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiMediaTypes.matchesSubtype(value, "xml"));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "text/xml",
            "application/xml",
            "text/xml; charset=utf-8",
        })
        void shouldBeTrue_WhenSubtypesMatch(String mediaType) {
            assertThat(KiwiMediaTypes.matchesSubtype(mediaType, "xml")).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "text/xml",
            "application/xml",
            "text/xml; charset=utf-8",
        })
        void shouldBeFalse_WhenSubtypesDoNotMatch(String mediaType) {
            assertThat(KiwiMediaTypes.matchesSubtype(mediaType, "json")).isFalse();
        }
    }
}
