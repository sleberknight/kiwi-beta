package org.kiwiproject.beta.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.kiwiproject.test.junit.jupiter.params.provider.AsciiOnlyBlankStringSource;

@DisplayName("KiwiHttpResponses2")
class KiwiHttpResponses2Test {

    @Test
    void shouldThrow_IllegalArgument_WhenResponseIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiHttpResponses2.htmlEscape(null, "text/html"))
                .withMessage("entity must not be null");
    }   

    @ParameterizedTest
    @AsciiOnlyBlankStringSource
    void shouldThrow_IllegalArgument_WhenMediaTypeIsBlank(String mediaType) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiHttpResponses2.htmlEscape("<p>Hello</p>", mediaType))
                .withMessage("mediaType must not be blank");
    } 

    @Test
    void shouldEscape_HtmlContent() {
        var text = KiwiHttpResponses2.htmlEscape("<p>Hello, world</p>", "text/html");
        
        assertThat(text).isEqualTo("&lt;p&gt;Hello, world&lt;/p&gt;");
    }

    @Test
    void shouldNotEscape_NonHtmlContent() {
        var text = KiwiHttpResponses2.htmlEscape("Hello, world", "text/plain");

        assertThat(text).isEqualTo("Hello, world");
    }

    @Test
    void shouldReturnEmptyString_WhenContentIsEmpty() {
       var text = KiwiHttpResponses2.htmlEscape("", "text/html");

        assertThat(text).isEmpty();
    }
}
