package org.kiwiproject.beta.jakarta.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.glassfish.jersey.internal.LocalizationMessages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.kiwiproject.test.okhttp3.mockwebserver.MockWebServerExtension;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@DisplayName("KiwiResponses2")
class KiwiResponses2Test {

    @RegisterExtension
    private final MockWebServerExtension serverExtension = new MockWebServerExtension();

    private MockWebServer server;
    private Client client;
    private URI baseUri;

    @BeforeEach
    void setUp() {
        client = ClientBuilder.newBuilder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .build();

        server = serverExtension.server();
        baseUri = serverExtension.uri();
    }

    @Test
    void shouldThrow_IllegalArgument_WhenResponseIsNull() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> KiwiResponses2.htmlEscapeEntity(null))
                .withMessage("response must not be null");
    }   

    @Test
    void shouldThrow_IllegalState_WhenResponseEntity_IsAlreadyConsumed() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                .setBody("<p>Hello, world</p>"));
        
        var response = makeRequest();

        // Consume the entity
        response.readEntity(String.class);

        // Now try to escape it
        assertThatIllegalStateException()
                .describedAs("Should get IllegalStateException when response entity has been consumed")
                .isThrownBy(() -> KiwiResponses2.htmlEscapeEntity(response))
                .withMessage(LocalizationMessages.ERROR_ENTITY_STREAM_CLOSED());
    }

     @Test
    void shouldThrow_IllegalState_WhenResponse_IsClosed() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                .setBody("<p>Hello, world</p>"));
        
        var response = makeRequest();
        
        // Close the response (without reading the entity)
        response.close();

        // Now try to escape it
        assertThatIllegalStateException()
                .describedAs("Should get IllegalStateException when response is closed")
                .isThrownBy(() -> KiwiResponses2.htmlEscapeEntity(response))
                .withMessage(LocalizationMessages.ERROR_ENTITY_STREAM_CLOSED());
    }

    @Test
    void shouldEscape_HtmlResponses() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "text/html")
                .setBody("<p>Hello, world</p>"));
        
        var response = makeRequest();

        var text = KiwiResponses2.htmlEscapeEntity(response);
        
        assertThat(text).isEqualTo("&lt;p&gt;Hello, world&lt;/p&gt;");
    }

    @Test
    void shouldNotEscape_NonHtmlResponses() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, "text/plain")
                .setBody("Hello, world"));
        
        var response = makeRequest();

        var text = KiwiResponses2.htmlEscapeEntity(response);

        assertThat(text).isEqualTo("Hello, world");
    }

    @Test
    void shouldReturnEmptyString_WhenResponseHasZeroLengthContent() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_LENGTH, 0));

        var response = makeRequest();

        var text = KiwiResponses2.htmlEscapeEntity(response);

        assertThat(text).isEmpty();
    }

    @Test
    void shouldReturnEmptyString_WhenResponseHasNoBody() {
        server.enqueue(new MockResponse()
                .setResponseCode(204));

        var response = makeRequest();

        var text = KiwiResponses2.htmlEscapeEntity(response);

        assertThat(text).isEmpty();
    }

    private Response makeRequest() {
        return client.target(baseUri).request().get();
    }
}
