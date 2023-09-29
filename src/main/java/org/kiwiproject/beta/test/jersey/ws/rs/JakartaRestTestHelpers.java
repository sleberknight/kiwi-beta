package org.kiwiproject.beta.test.jersey.ws.rs;

import static org.mockito.AdditionalAnswers.answer;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

import com.google.common.annotations.Beta;

import lombok.experimental.UtilityClass;

import org.mockito.ArgumentMatchers;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

/**
 * Test utilities for Jakarta RESTful Web Services.
 */
@UtilityClass
@Beta
public class JakartaRestTestHelpers {

    /**
     * Converts an <em>outbound</em> {@link Response} built as if from inside a Jakarta EE resource, e.g. something like
     * {@code Response.ok(entity).build()}, into an <em>inbound</em> Response on which {@link Response#readEntity(Class)}
     * can be called. Inbound responses are what Jakarta Clients return, and the {@code readEntity} methods are
     * how client code obtains the response entity.
     * <p>
     * This is useful if you are testing client code and you want to mock the response returned by an endpoint in order
     * to test how the client responds to various responses, e.g. different error conditions. If you don't do this
     * and your test attempts to read an entity from an outbound response, an IllegalStateException is thrown.
     * <p>
     * Note that in this implementation, only {@link Response#readEntity(Class)} and
     * {@link Response#readEntity(GenericType)} will return the entity. The other two {@code readEntity} methods
     * that accept an array of Annotation will throw an exception.
     * <p>
     * All the credit goes to Ashley Frieze for this solution. See his blog entry on this:
     * <a href="https://codingcraftsman.wordpress.com/2018/11/26/testing-and-mocking-jersey-responses/">Testing and Mocking Jersey Responses</a>
     *
     * @param outboundResponse the outbound response
     * @return a simulated inbound response
     * @implNote The implementation uses Mockito to spy the outbound response, essentially intercepting calls to
     * readEntity and returning the value returned by {@link Response#getEntity()} instead.
     */
    public static Response toInboundResponse(Response outboundResponse) {
        var inboundResponse = spy(outboundResponse);

        // handle readEntity(Class)
        doAnswer(answer((Class<?> type) -> readEntity(inboundResponse)))
                .when(inboundResponse)
                .readEntity(ArgumentMatchers.<Class<?>>any());

        // handle readEntity(GenericType)
        doAnswer(answer((GenericType<?> type) -> readEntity(inboundResponse)))
                .when(inboundResponse)
                .readEntity(ArgumentMatchers.<GenericType<?>>any());

        return inboundResponse;
    }

    @SuppressWarnings({"unchecked"})
    private static <T> T readEntity(Response realResponse) {
        return (T) realResponse.getEntity();
    }
}
