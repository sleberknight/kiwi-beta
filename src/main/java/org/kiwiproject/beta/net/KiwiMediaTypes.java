package org.kiwiproject.beta.net;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;
import com.google.common.net.MediaType;
import lombok.experimental.UtilityClass;

/**
 * Some utilities for working with <a href="https://en.wikipedia.org/wiki/Media_type">media types</a>.
 * <p>
 * Google Guava contains the <a href="https://javadoc.io/doc/com.google.guava/guava/latest/com/google/common/net/MediaType.html">MediaType</a>
 * class which represents an Internet Media Type. It contains some useful methods, but not some (what we think are)
 * basic utilities such as the ability to check if a media type is XML. Both {@code text/xml} and
 * {@code application/xml} represent XML documents, so checking whether a media type is XML should take this into
 * account. But the Guava {@code MediaType#is} method doesn't permit comparing like this when the type is different
 * but the subtype is the same.
 * <p>
 * The Jakarta RS <a href="https://javadoc.io/doc/jakarta.ws.rs/jakarta.ws.rs-api/latest/jakarta.ws.rs/jakarta/ws/rs/core/MediaType.html">MediaType</a>
 * is also useful, but its {@code equals} method unfortunately compares type, subtype, <em>and parameters</em> so
 * you cannot use it to check if a media type such as "text/xml; charset=utf-8" equals the "text/xml" media type,
 * since it will include the charset parameter in the comparison and return false.
 *
 * @implNote Internally this uses Guava's {@code MediaType} to parse String media types.
 */
@UtilityClass
@Beta
public class KiwiMediaTypes {

    private static final String APPLICATION_TYPE = "application";
    private static final String TEXT_TYPE = "text";
    private static final String JSON_SUBTYPE = "json";
    private static final String XML_SUBTYPE = "xml";

    /**
     * Checks if media type is "application/xml" or "text/xml", ignoring parameters such that
     * "text/xml; charset=utf-8" is considered XML.
     * <p>
     * To use this method, the jakarta.ws.rs:jakarta.ws.rs-api dependency must be present.
     *
     * @param mediaType the media type to check
     * @return true if the media type is an XML type ignoring any parameters, otherwise false
     */
    public static boolean isXml(jakarta.ws.rs.core.MediaType mediaType) {
        checkArgumentNotNull(mediaType, "mediaType must not be null");
        return isXml(toStringWithoutParameters(mediaType));
    }

    /**
     * Checks if media type is "application/xml" or "text/xml", ignoring parameters such that
     * "text/xml; charset=utf-8" is considered XML.
     *
     * @param mediaType the media type to check
     * @return true if the media type is an XML type ignoring any parameters, otherwise false
     */
    public static boolean isXml(String mediaType) {
        checkMediaTypeNotBlank(mediaType);
        var parsedType = MediaType.parse(mediaType);
        var type = parsedType.type();
        var subtype = parsedType.subtype();
        return (TEXT_TYPE.equals(type) || APPLICATION_TYPE.equals(type)) && XML_SUBTYPE.equals(subtype);
    }

    /**
     * Checks if media type is "application/json", ignoring parameters such that "application/json; charset=utf-8"
     * is considered JSON.
     * <p>
     * To use this method, the jakarta.ws.rs:jakarta.ws.rs-api dependency must be present.
     *
     * @param mediaType the media type to check
     * @return true if the media type is JSON ignoring any parameters, otherwise false
     */
    public static boolean isJson(jakarta.ws.rs.core.MediaType mediaType) {
        checkArgumentNotNull(mediaType, "mediaType must not be null");
        return isJson(toStringWithoutParameters(mediaType));
    }

    /**
     * Get the string value of the given {@link jakarta.ws.rs.core.MediaType} with only the type/subtype.
     *
     * @implNote This method concatenates the type and subtype of the MediaType because the
     * MediaType#toString requires a Jakarta RS implementation in order to create a RuntimeDelegate
     * which is then used to convert to a String. Presumably if this method is used, the implementation
     * is available, but just in case it isn't, this method manually creates the media type string using
     * the type and subtype, since they are just fields in MediaType and don't need a Jakarta RS
     * implementation to be available.
     */
    public static String toStringWithoutParameters(jakarta.ws.rs.core.MediaType mediaType) {
        return f("{}/{}", mediaType.getType(), mediaType.getSubtype());
    }

    /**
     * Checks if media type is "application/json", ignoring parameters such that "application/json; charset=utf-8"
     * is considered JSON.
     *
     * @param mediaType the media type to check
     * @return true if the media type is JSON ignoring any parameters, otherwise false
     */
    public static boolean isJson(String mediaType) {
        checkMediaTypeNotBlank(mediaType);
        var parsedType = MediaType.parse(mediaType);
        var type = parsedType.type();
        var subtype = parsedType.subtype();
        return APPLICATION_TYPE.equals(type) && JSON_SUBTYPE.equals(subtype);
    }

    /**
     * Checks if the given media type has a type that matches the given value.
     *
     * @param mediaType the media type to check
     * @param typeToMatch the type to match
     * @return true if the types match, otherwise false
     */
    public static boolean matchesType(String mediaType, String typeToMatch) {
        checkMediaTypeNotBlank(mediaType);
        checkArgumentNotBlank(typeToMatch, "typeToMatch must not be blank");
        var parsedType = MediaType.parse(mediaType);
        var type = parsedType.type();
        return typeToMatch.equals(type);
    }

    /**
     * Checks if the given media type has a type that matches the given value.
     *
     * @param mediaType the media type to check
     * @param subtypeToMatch the subtype to match
     * @return true if the subtypes match, otherwise false
     */
    public static boolean matchesSubtype(String mediaType, String subtypeToMatch) {
        checkMediaTypeNotBlank(mediaType);
        checkArgumentNotBlank(subtypeToMatch, "subtypeToMatch must not be blank");
        var parsedType = MediaType.parse(mediaType);
        var subtype = parsedType.subtype();
        return subtypeToMatch.equals(subtype);
    }

    private static void checkMediaTypeNotBlank(String mediaType) {
        checkArgumentNotBlank(mediaType, "mediaType must not be blank");
    }
}
