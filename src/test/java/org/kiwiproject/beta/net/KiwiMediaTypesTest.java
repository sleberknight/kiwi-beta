package org.kiwiproject.beta.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import com.google.common.collect.Sets;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;
import org.kiwiproject.test.junit.jupiter.params.provider.MinimalBlankStringSource;

import java.util.Set;

@DisplayName("KiwiMediaTypes")
class KiwiMediaTypesTest {

    @Nested
    class IsXml {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isXml(value))
                    .withMessage("mediaType must not be blank");
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
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isXml((MediaType) null))
                    .withMessage("mediaType must not be null");
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
    class IsHtml {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isHtml(value))
                    .withMessage("mediaType must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "text/html",
            "text/html; charset=utf-8",
            "text/html; charset=ISO-8859-1",
        })
        void shouldBeTrue_WhenGivenAnAcceptableHtmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isHtml(mediaType)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/xml",
            "text/css",
            "application/json; charset=utf-8",
            "text/xml; charset=ISO-8859-1",
            "image/jpeg"
        })
        void shouldBeFalse_WhenNotHtmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isHtml(mediaType)).isFalse();
        }
    }

    @Nested
    class IsHtmlWithJakartaMediaType {

        @Test
        void shouldNotAllowNullMediaType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isHtml((MediaType) null))
                    .withMessage("mediaType must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "text/html",
            "text/html; charset=utf-8",
            "text/html; charset=ISO-8859-1",
        })
        void shouldBeTrue_WhenGivenAnAcceptableHtmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isHtml(MediaType.valueOf(mediaType))).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/xml",
            "text/css",
            "application/json; charset=utf-8",
            "text/xml; charset=ISO-8859-1",
            "image/jpeg"
        })
        void shouldBeFalse_WhenNotHtmlType(String mediaType) {
            assertThat(KiwiMediaTypes.isHtml(MediaType.valueOf(mediaType))).isFalse();
        }
    }

    @Nested
    class IsJson {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isJson(value))
                    .withMessage("mediaType must not be blank");
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
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isJson((MediaType) null))
                    .withMessage("mediaType must not be null");
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

    @Nested
    class IsPlainText {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isPlainText(value))
                    .withMessage("mediaType must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "text/plain",
            "text/plain; charset=utf-8",
            "text/plain; version=0.0.4; charset=utf-8",
            "text/plain; charset=ISO-8859-1",
        })
        void shouldBeTrue_WhenGivenAnAcceptablePlainTextType(String mediaType) {
            assertThat(KiwiMediaTypes.isPlainText(mediaType)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/xml",
            "text/css",
            "text/html; charset=utf-8",
            "text/xml; charset=ISO-8859-1",
            "image/jpeg"
        })
        void shouldBeFalse_WhenNotPlainTxtType(String mediaType) {
            assertThat(KiwiMediaTypes.isPlainText(mediaType)).isFalse();
        }
    }

    @Nested
    class IsPlainTextWithJakartaMediaType {

        @Test
        void shouldNotAllowNullMediaType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.isPlainText((MediaType) null))
                    .withMessage("mediaType must not be null");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "text/plain",
            "text/plain; charset=utf-8",
            "text/plain; version=0.0.4; charset=utf-8",
            "text/plain; charset=ISO-8859-1",
        })
        void shouldBeTrue_WhenGivenAnAcceptablePlainTextType(String mediaType) {
            assertThat(KiwiMediaTypes.isPlainText(MediaType.valueOf(mediaType))).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application/xml",
            "text/css",
            "text/html; charset=utf-8",
            "text/xml; charset=ISO-8859-1",
            "image/jpeg"
        })
        void shouldBeFalse_WhenNotPlainTxtType(String mediaType) {
            assertThat(KiwiMediaTypes.isPlainText(MediaType.valueOf(mediaType))).isFalse();
        }
    }

    @Nested
    class ToStringWithoutParameters {

        @Test
        void shouldNowAllowNullArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.toStringWithoutParameters(null))
                    .withMessage("mediaType must not be null");
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                text/xml, text/xml
                text/xml; charset=utf-8, text/xml
                application/xml, application/xml
                application/xml; charset=utf-8, application/xml
                application/json, application/json
                application/json; charset=utf-8, application/json
                text/html; charset=utf-8, text/html
                text/html; charset=ISO-8859-1, text/html
                'text/plain; version=0.0.4; charset=utf-8', text/plain
                """)
        void shouldReturnJakartaMediaTypeAsStringWithoutParameters(String mediaType, String expectedResult) {
            var jakartaMediaType = MediaType.valueOf(mediaType);
            var plainMediaType = KiwiMediaTypes.toStringWithoutParameters(jakartaMediaType);

            assertThat(plainMediaType).isEqualTo(expectedResult);
        }
    }

    @Nested
    class WithoutParameters {

        @ParameterizedTest
        @MinimalBlankStringSource
        void shouldNotAllowBlankArguments(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.withoutParameters(value))
                    .withMessage("mediaType must not be blank");
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
                text/xml, text/xml
                text/xml; charset=utf-8, text/xml
                application/xml, application/xml
                application/xml; charset=utf-8, application/xml
                application/json, application/json
                application/json; charset=utf-8, application/json
                text/html; charset=utf-8, text/html
                text/html; charset=ISO-8859-1, text/html
                'text/plain; version=0.0.4; charset=utf-8', text/plain
                """)
        void shouldStripParameters(String mediaType, String expectedResult) {
            var plainMediaType = KiwiMediaTypes.withoutParameters(mediaType);
            assertThat(plainMediaType).isEqualTo(expectedResult);
        }
    }

    @Nested
    class MatchesType {

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankMediaType(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesType(value, "text"))
                    .withMessage("mediaType must not be blank");
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
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesSubtype(value, "xml"))
                    .withMessage("mediaType must not be blank");
        }

        @ParameterizedTest
        @AsciiOnlyBlankStringSource
        void shouldNotAllowBlankSubtypeTypeToMatch(String value) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesSubtype("application/json", value))
                    .withMessage("subtypeToMatch must not be blank");
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

    @Nested
    class MatchesMediaTypeWithStrings {

        @ParameterizedTest
        @CsvSource(textBlock = """
            null, null, mediaType must not be blank
            '', '', mediaType must not be blank
            null, text/plain, mediaType must not be blank
            '', text/plain, mediaType must not be blank
            text/plain, null, mediaTypeToMatch must not be blank
            text/plain, '', mediaTypeToMatch must not be blank
            """,
            nullValues = "null")
        void shouldNotAllowBlankArguments(String mediaType, String mediaTypeToMatch, String expectedMessage) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesMediaType(mediaType, mediaTypeToMatch))
                    .withMessage(expectedMessage);
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            application/json, application/json, true
            text/xml, text/xml, true
            text/plain, text/plain, true

            text/xml, application/xml, false
            application/json, application/xml, false
            text/plain, application/json, false
            """,
            nullValues = "null")
        void shouldBeTrue_WhenTypeAndSubtypeMatch(String mediaType, String mediaTypeToMatch, boolean expectMatch) {
            assertThat(KiwiMediaTypes.matchesMediaType(mediaType, mediaTypeToMatch))
                    .isEqualTo(expectMatch);
        }
    }

    @Nested
    class MatchesMediaTypeWithJakartaMediaTypeAndString {

        @Test
        void shouldNotAllowNullJakartaMediaType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesMediaType((MediaType) null, "application/json"))
                    .withMessage("mediaType must not be null");
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldNotAllowBlankMediaTypeToMatch(String mediaTypeToMatch) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesMediaType(MediaType.APPLICATION_JSON_TYPE, mediaTypeToMatch))
                    .withMessage("mediaTypeToMatch must not be blank");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "application",
            "json",
            "text plain",
            "application/json;",
            "application/json; charset=utf-8",
            "text/plain; version=0.0.4; charset=utf-8"
        })
        void shouldNotAllowMalformedMediaTypeToMatch(String mediaTypeToMatch) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesMediaType(MediaType.APPLICATION_JSON_TYPE, mediaTypeToMatch))
                    .withMessage("mediaTypeToMatch must not be blank and be in the format type/subtype");
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            application/json, application/json, true
            text/xml, text/xml, true
            text/plain, text/plain, true

            text/xml, application/xml, false
            application/json, application/xml, false
            text/plain, application/json, false
            """,
            nullValues = "null")
        void shouldBeTrue_WhenTypeAndSubtypeMatch(String mediaType, String mediaTypeToMatch, boolean expectMatch) {
            var jakartaMediaType = MediaType.valueOf(mediaType);

            assertThat(KiwiMediaTypes.matchesMediaType(jakartaMediaType, mediaTypeToMatch))
                    .isEqualTo(expectMatch);
        }
    }

    @Nested
    class MatchesMediaTypeWithJakartaMediaTypes {

        @Test
        void shouldNotAllowNullJakartaMediaType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesMediaType(null, MediaType.valueOf("application/json")))
                    .withMessage("mediaType must not be null");
        }

        @Test
        void shouldNotAllowNullJakartaMediaTypeToMatch() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesMediaType(MediaType.valueOf("application/json"), (MediaType) null))
                    .withMessage("mediaTypeToMatch must not be null");
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            application/json, application/json, true
            application/json;charset=UTF-8, application/json, true
            text/xml, text/xml, true
            text/html;charset=ISO-8859-1, text/html, true
            text/plain, text/plain, true

            text/xml, application/xml, false
            application/json, application/xml, false
            text/plain, application/json, false
            """,
            nullValues = "null")
        void shouldBeTrue_WhenTypeAndSubtypeMatch(String mediaType, String mediaTypeToMatch, boolean expectMatch) {
            assertThat(KiwiMediaTypes.matchesMediaType(MediaType.valueOf(mediaType),  MediaType.valueOf(mediaTypeToMatch)))
                    .isEqualTo(expectMatch);
        }
    }

    @Nested
    class MatchesTypeAndSubtype {

        @ParameterizedTest
        @CsvSource(textBlock = """
            null, null, null, typeToMatch must not be blank
            null, text, xml, mediaType must not be blank
            '', text, xml, mediaType must not be blank
            text/plain, null, plain, typeToMatch must not be blank
            text/plain, '', plain, typeToMatch must not be blank
            application/json, application, null, subtypeToMatch must not be blank
            application/json, application, '', subtypeToMatch must not be blank
            """,
            nullValues = "null")
        void shouldNotAllowBlankArguments(String mediaType, String typeToMatch, String subtypeToMatch, String expectedMessage) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype(mediaType, typeToMatch, subtypeToMatch))
                    .withMessage(expectedMessage);
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            application/json, application, json, true
            text/xml, text, xml, true
            text/plain, text, plain, true

            text/xml, application, xml, false
            application/json, application, xml, false
            text/plain, application, json, false
            """,
            nullValues = "null")
        void shouldBeTrue_WhenTypeAndSubtypeMatch(String mediaType, String type, String subtype, boolean expectMatch) {
            assertThat(KiwiMediaTypes.matchesTypeAndSubtype(mediaType, type, subtype))
                    .isEqualTo(expectMatch);
        }
    }

    @Nested
    class MatchesTypeAndSubtypeWithJakartaMediaType {

        @Test
        void shouldNotAllowNullJakartaMediaType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype((MediaType) null, "application", "json"))
                    .withMessage("mediaType must not be null");
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            null, null, typeToMatch must not be blank
            null, plain, typeToMatch must not be blank
            '', plain, typeToMatch must not be blank
            application, null, subtypeToMatch must not be blank
            application, '', subtypeToMatch must not be blank
            """,
            nullValues = "null")
        void shouldNotAllowBlankArguments(String typeToMatch, String subtypeToMatch, String expectedMessage) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype(MediaType.APPLICATION_JSON_TYPE, typeToMatch, subtypeToMatch))
                    .withMessage(expectedMessage);
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            application/json, application, json, true
            text/xml, text, xml, true
            text/plain, text, plain, true

            text/xml, application, xml, false
            application/json, application, xml, false
            text/plain, application, json, false
            """,
            nullValues = "null")
        void shouldBeTrue_WhenTypeAndSubtypeMatch(String mediaType, String type, String subtype, boolean expectMatch) {
            var jakartaMediaType = MediaType.valueOf(mediaType);
            assertThat(KiwiMediaTypes.matchesTypeAndSubtype(jakartaMediaType, type, subtype))
                    .isEqualTo(expectMatch);
        }
    }

    @Nested
    class MatchesTypeAndSubtype_WithSetOfTypes {

        @ParameterizedTest
        @CsvSource(textBlock = """
            null, null, mediaType must not be blank
            '', '', mediaType must not be blank
            null, xml, mediaType must not be blank
            '', xml, mediaType must not be blank
            text/plain, null, subtypeToMatch must not be blank
            text/plain, '', subtypeToMatch must not be blank
            """,
            nullValues = "null")
        void shouldNotAllowBlankArguments(String mediaType, String subtypeToMatch, String expectedMessage) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype(mediaType, Set.of("text"), subtypeToMatch))
                    .withMessage(expectedMessage);
        }

        @Test
        void shouldNotAllowEmptyTypesToMatch() {
            Set<String> typesToMatch = Set.of();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype("text/xml", typesToMatch, "xml"))
                    .withMessage("typesToMatch must not be empty");
        }

        @ParameterizedTest
        @MinimalBlankStringSource
        void shouldNotAllowBlankElementsInTypesToMatch(String value) {
            var typesToMatch = Sets.newHashSet("text", value);  // use Guava's Sets b/c Set#of rejects null
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype("text/xml", typesToMatch, "xml"))
                    .withMessage("typesToMatch must not contain blank elements");
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            application/json, application, json, true
            text/xml, text, xml, true
            text/plain, text, plain, true

            text/xml, application, xml, false
            application/json, application, xml, false
            text/plain, application, json, false
            """,
            nullValues = "null")
        void shouldBeTrue_WhenTypeAndSubtypeMatch(String mediaType, String type, String subtype, boolean expectMatch) {
            var typesToMatch = Set.of(type);
            assertThat(KiwiMediaTypes.matchesTypeAndSubtype(mediaType, typesToMatch, subtype))
                    .isEqualTo(expectMatch);
        }
    }

    @Nested
    class MatchesTypeAndSubtype_WithSetOfTypes_WithJakartaMediaType {

        @Test
        void shouldNotAllowNullJakartaMediaType() {
            var typesToMatch = Set.of("application");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype((MediaType) null, typesToMatch, "json"))
                    .withMessage("mediaType must not be null");
        }

        @Test
        void shouldNotAllowEmptyTypesToMatch() {
            Set<String> typesToMatch = Set.of();
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype(MediaType.TEXT_XML_TYPE, typesToMatch, "xml"))
                    .withMessage("typesToMatch must not be empty");
        }

        @ParameterizedTest
        @MinimalBlankStringSource
        void shouldNotAllowBlankElementsInTypesToMatch(String value) {
            var typesToMatch = Sets.newHashSet("text", value);  // use Guava's Sets b/c Set#of rejects null
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype(MediaType.TEXT_XML_TYPE, typesToMatch, "xml"))
                    .withMessage("typesToMatch must not contain blank elements");
        }

        @ParameterizedTest
        @MinimalBlankStringSource
        void shouldNotAllowBlankSubtypeToMatch(String value) {
            var typesToMatch = Set.of("application");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiMediaTypes.matchesTypeAndSubtype(MediaType.APPLICATION_JSON_TYPE, typesToMatch, value))
                    .withMessage("subtypeToMatch must not be blank");
        }

        @ParameterizedTest
        @CsvSource(textBlock = """
            application/json, application, json, true
            text/xml, text, xml, true
            text/plain, text, plain, true

            text/xml, application, xml, false
            application/json, application, xml, false
            text/plain, application, json, false
            """,
            nullValues = "null")
        void shouldBeTrue_WhenTypeAndSubtypeMatch(String mediaType, String type, String subtype, boolean expectMatch) {
            var jakartaMediaType = MediaType.valueOf(mediaType);
            var typesToMatch = Set.of(type);
            assertThat(KiwiMediaTypes.matchesTypeAndSubtype(jakartaMediaType, typesToMatch, subtype))
                    .isEqualTo(expectMatch);
        }
    }
}
