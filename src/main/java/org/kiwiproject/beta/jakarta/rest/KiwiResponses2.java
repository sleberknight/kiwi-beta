package org.kiwiproject.beta.jakarta.rest;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import jakarta.ws.rs.core.Response;
import lombok.experimental.UtilityClass;
import org.kiwiproject.beta.net.KiwiHttpResponses2;

/**
 * Utilities related to Jakarta REST {@link Response}.
 * <p>
 * Some of these methods eventually may be moved into {@code org.kiwiproject.jaxrs.KiwiResponses}
 * in <a href="https://github.com/kiwiproject/kiwi">kiwi</a>.
 */
@UtilityClass
@Beta
public class KiwiResponses2 {

    /**
     * If the {@link Response} is HTML, return the escaped entity.
     * Otherwise, return the original response entity.
     * <p>
     * If the response has no entity, an empty string is returned to be
     * consistent with the behavior of {@link Response} when reading
     * the entity as a String.
     * <p>
     * <strong>Note:</strong>
     * The {@code response} must not be closed, and the entity cannot
     * have already been read. These are transitive restrictions from
     * the {@link Response#readEntity(Class) readEntity} method, which
     * is used to read the entity. An {@code IllegalStateException} is
     * thrown if either of these restrictions is violated. Also note that
     * the response will be closed after this method returns, again
     * because of {@code readEntity}.
     *
     * @param response the {@link Response} from which to read and escape the entity
     * @return the HTML-escaped entity if the response is HTML, otherwise the original entity
     * @throws IllegalArgumentException if {@code response} is null
     * @throws IllegalStateException if the {@code response} is closed or the entity has
     * already been consumed
     * @see Response#readEntity(Class)
     */
    public static String htmlEscapeEntity(Response response) {
        checkArgumentNotNull(response, "response must not be null");

        var entity = response.readEntity(String.class);
        var mediaType = response.getMediaType();
        if (nonNull(mediaType)) {
            return KiwiHttpResponses2.htmlEscape(entity, mediaType.toString());
        }
        return entity;
    }
}
