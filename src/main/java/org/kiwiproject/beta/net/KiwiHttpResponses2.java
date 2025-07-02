package org.kiwiproject.beta.net;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.html.HtmlEscapers;
import lombok.experimental.UtilityClass;

/**
 * Utilities related to HTTP responses.
 * <p>
 * Some of these methods may eventually be moved into {@code org.kiwiproject.net.KiwiHttpResponses}
 * in <a href="https://github.com/kiwiproject/kiwi">kiwi</a>. 
 */
@UtilityClass
public class KiwiHttpResponses2 {

    /**
     * If the {@code mediaType} is HTML ({@code text/html}), return the escaped entity.
     * Otherwise, return {@code entity} without modification.
     * 
     * @param entity the response entity
     * @param mediaType the media type to evaluate
     * @return the HTML-escaped entity if the response is HTML, otherwise the original entity
     * @throws IllegalArgumentException if {@code entity} is null or {@code mediaType} is blank
     * @see HtmlEscapers
     */
    public static String htmlEscape(String entity, String mediaType) {
        checkArgumentNotNull(entity, "entity must not be null");
        checkArgumentNotBlank(mediaType, "mediaType must not be blank");

        if (KiwiMediaTypes.isHtml(mediaType)) {
            return HtmlEscapers.htmlEscaper().escape(entity);
        }

        return entity;
    }
}
