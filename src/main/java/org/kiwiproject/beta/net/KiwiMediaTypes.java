package org.kiwiproject.beta.net;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.indexOf;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotEmpty;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;
import com.google.common.net.MediaType;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * Some utilities for working with <a href="https://en.wikipedia.org/wiki/Media_type">media types</a>.
 * Mozilla also has a good reference that describes
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types">MIME types (IANA media types)</a>.
 * <p>
 * Google Guava contains the <a href="https://javadoc.io/doc/com.google.guava/guava/latest/com/google/common/net/MediaType.html">MediaType</a>
 * class which represents an Internet Media Type. It contains some useful methods, but not some (what we think are)
 * basic utilities such as the ability to check if a media type is XML. Both {@code text/xml} and
 * {@code application/xml} represent XML documents, so checking whether a media type is XML should take this into
 * account. But the Guava {@code MediaType#is} method doesn't permit comparing like this when the type is different
 * but the subtype is the same.
 * <p>
 * The Jakarta Rest <a href="https://javadoc.io/doc/jakarta.ws.rs/jakarta.ws.rs-api/latest/jakarta.ws.rs/jakarta/ws/rs/core/MediaType.html">MediaType</a>
 * is also useful, but its {@code equals} method unfortunately compares type, subtype, <em>and parameters</em> so
 * you cannot use it to check if a media type such as "text/xml; charset=utf-8" equals the "text/xml" media type,
 * since it will include the charset parameter in the comparison and return false.
 *
 * @implNote Internally this uses Guava's {@link MediaType} to parse String media types.
 */
@UtilityClass
@Beta
public class KiwiMediaTypes {

    private static final String APPLICATION_TYPE = "application";
    private static final String TEXT_TYPE = "text";
    private static final Set<String> XML_TYPES = Set.of(TEXT_TYPE, APPLICATION_TYPE);

    private static final String JSON_SUBTYPE = "json";
    private static final String PLAIN_SUBTYPE = "plain";
    private static final String XML_SUBTYPE = "xml";

    /**
     * Checks if the Jakarta Rest media type is "application/xml" or "text/xml", ignoring parameters such that
     * "text/xml; charset=utf-8" is considered XML.
     * <p>
     * To use this method,
     * the <a href="https://mvnrepository.com/artifact/jakarta.ws.rs/jakarta.ws.rs-api">jakarta.ws.rs:jakarta.ws.rs-api</a>
     * dependency must be present.
     *
     * @param mediaType the Jakarta Rest media type to check
     * @return true if the media type is an XML type ignoring any parameters, otherwise false
     */
    public static boolean isXml(jakarta.ws.rs.core.MediaType mediaType) {
        checkJakartaMediaType(mediaType);
        return isXml(mediaType.toString());
    }

    /**
     * Checks if the media type is "application/xml" or "text/xml", ignoring parameters such that
     * "text/xml; charset=utf-8" is considered XML.
     *
     * @param mediaType the media type to check
     * @return true if the media type is an XML type ignoring any parameters, otherwise false
     */
    public static boolean isXml(String mediaType) {
        return matchesTypeAndSubtype(mediaType, XML_TYPES, XML_SUBTYPE);
    }

    /**
     * Checks if the Jakarta Rest media type is "application/json", ignoring parameters such
     * that "application/json; charset=utf-8" is considered JSON.
     * <p>
     * To use this method,
     * the <a href="https://mvnrepository.com/artifact/jakarta.ws.rs/jakarta.ws.rs-api">jakarta.ws.rs:jakarta.ws.rs-api</a>
     * dependency must be present.
     *
     * @param mediaType the Jakarta Rest media type to check
     * @return true if the media type is JSON ignoring any parameters, otherwise false
     */
    public static boolean isJson(jakarta.ws.rs.core.MediaType mediaType) {
        checkJakartaMediaType(mediaType);
        return isJson(mediaType.toString());
    }

    /**
     * Checks if the media type is "application/json", ignoring parameters such
     * that "application/json; charset=utf-8" is considered JSON.
     *
     * @param mediaType the media type to check
     * @return true if the media type is JSON ignoring any parameters, otherwise false
     */
    public static boolean isJson(String mediaType) {
        return matchesTypeAndSubtype(mediaType, APPLICATION_TYPE, JSON_SUBTYPE);
    }

    /**
     * Checks if the Jakarta Rest media type is "text/plain", ignoring parameters such
     * that "text/plain; version=0.0.4; charset=utf-8" is considered plain text.
     * <p>
     * To use this method,
     * the <a href="https://mvnrepository.com/artifact/jakarta.ws.rs/jakarta.ws.rs-api">jakarta.ws.rs:jakarta.ws.rs-api</a>
     * dependency must be present.
     *
     * @param mediaType the Jakarta Rest media type to check
     * @return true if the media type is plain text ignoring any parameters, otherwise false
     */
    public static boolean isPlainText(jakarta.ws.rs.core.MediaType mediaType) {
        checkJakartaMediaType(mediaType);
        return isPlainText(mediaType.toString());
    }

    /**
     * Checks if the media type is "text/plain", ignoring parameters such
     * that "text/plain; version=0.0.4; charset=utf-8" is considered plain text.
     *
     * @param mediaType the media type to check
     * @return true if the media type is plain text ignoring any parameters, otherwise false
     */
    public static boolean isPlainText(String mediaType) {
        return matchesTypeAndSubtype(mediaType, TEXT_TYPE, PLAIN_SUBTYPE);
    }

    /**
     * Get the string value of the given {@link jakarta.ws.rs.core.MediaType} with only the type/subtype.
     * <p>
     * To use this method,
     * the <a href="https://mvnrepository.com/artifact/jakarta.ws.rs/jakarta.ws.rs-api">jakarta.ws.rs:jakarta.ws.rs-api</a>
     * dependency must be present.
     *
     * @param mediaType the Jakarta Rest media type to strip parameters from
     * @implNote This method concatenates the type and subtype of the MediaType because the
     * MediaType#toString requires a Jakarta RS implementation to create a RuntimeDelegate
     * which is then used to convert to a String. Presumably, if this method is used, the implementation
     * is available. However, just in case it isn't, this method manually creates the media type string using
     * the type and subtype, since they are just fields in MediaType and don't need a Jakarta RS
     * implementation to be available.
     */
    public static String toStringWithoutParameters(jakarta.ws.rs.core.MediaType mediaType) {
        checkJakartaMediaType(mediaType);
        return f("{}/{}", mediaType.getType(), mediaType.getSubtype());
    }

    /**
     * Strip any parameters from {@code mediaType}, returning a value in the format {@code type/subtype}.
     * <p>
     * This is a convenience method that delegates to Guava's {@link MediaType} and which handles
     * parsing the String value to a {@link MediaType}, removing the parameters, and converting to a String.
     *
     * @param mediaType the media type to strip parameters from
     * @return the "plain" media type as {@code type/subtype}
     * @see MediaType#parse(String)
     * @see MediaType#withoutParameters
     */
    public static String withoutParameters(String mediaType) {
        checkMediaTypeNotBlank(mediaType);
        return MediaType.parse(mediaType).withoutParameters().toString();
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

    /**
     * Checks if the given media type has a type and subtype that matches the given {@code type/subtype} media type.
     * Ignores parameters in {@code mediaType} such as "version" and "charset" in
     * {@code text/plain; version=0.0.4; charset=utf-8}.
     * <p>
     * To use this method,
     * the <a href="https://mvnrepository.com/artifact/jakarta.ws.rs/jakarta.ws.rs-api">jakarta.ws.rs:jakarta.ws.rs-api</a>
     * dependency must be present.
     *
     * @param mediaType the Jakarta Rest media type to check
     * @param mediaTypeToMatch the media type to match (must be in exact format {@code type/subtype})
     * @return true if the type and subtype both match ignoring parameters, otherwise false
     */
    public static boolean matchesMediaType(jakarta.ws.rs.core.MediaType mediaType, String mediaTypeToMatch) {
        checkJakartaMediaType(mediaType);
        return matchesMediaType(mediaType.toString(), mediaTypeToMatch);
    }

    /**
     * Checks if the given media type has a type and subtype that matches the given {@code type/subtype} media type.
     * Ignores parameters in {@code mediaType} such as "version" and "charset" in
     * {@code text/plain; version=0.0.4; charset=utf-8}.
     *
     * @param mediaType the media type to check
     * @param mediaTypeToMatch the media type to match (must be in exact format {@code type/subtype})
     * @return true if the type and subtype both match ignoring parameters, otherwise false
     */
    public static boolean matchesMediaType(String mediaType, String mediaTypeToMatch) {
        checkMediaTypeNotBlank(mediaType);

        var slashIndex = indexOf(mediaTypeToMatch, '/');
        checkArgument(slashIndex > -1 && !contains(mediaTypeToMatch, ';'),
                "mediaTypeToMatch must not be blank and be in the format type/subtype");

        var type = mediaTypeToMatch.substring(0, slashIndex);
        var subtype = mediaTypeToMatch.substring(slashIndex + 1);
        return matchesTypeAndSubtype(mediaType, type, subtype);
    }

    /**
     * Checks if the given media type has a type and subtype that matches the given values.
     * <p>
     * To use this method,
     * the <a href="https://mvnrepository.com/artifact/jakarta.ws.rs/jakarta.ws.rs-api">jakarta.ws.rs:jakarta.ws.rs-api</a>
     * dependency must be present.
     *
     * @param mediaType the Jakarta Rest media type to check
     * @param typeToMatch the type to match
     * @param subtypeToMatch the subtype to match
     * @return true if the type and subtype both match, otherwise false
     */
    public static boolean matchesTypeAndSubtype(jakarta.ws.rs.core.MediaType mediaType,
                                                String typeToMatch,
                                                String subtypeToMatch) {
        checkJakartaMediaType(mediaType);
        return matchesTypeAndSubtype(mediaType.toString(), typeToMatch, subtypeToMatch);
    }

    /**
     * Checks if the given media type has a type and subtype that matches the given values.
     *
     * @param mediaType the media type to check
     * @param typeToMatch the type to match
     * @param subtypeToMatch the subtype to match
     * @return true if the type and subtype both match, otherwise false
     */
    public static boolean matchesTypeAndSubtype(String mediaType, String typeToMatch, String subtypeToMatch) {
        checkArgumentNotBlank(typeToMatch, "typeToMatch must not be blank");
        return matchesTypeAndSubtype(mediaType, Set.of(typeToMatch), subtypeToMatch);
    }

    /**
     * Checks if the given media type has a type and subtype that matches the given values.
     * <p>
     * This method lets you test for subtypes which can have more than one type.
     * For example, "application/xml" and "text/xml" are both considered valid XML types.
     * <p>
     * To use this method,
     * the <a href="https://mvnrepository.com/artifact/jakarta.ws.rs/jakarta.ws.rs-api">jakarta.ws.rs:jakarta.ws.rs-api</a>
     * dependency must be present.
     *
     * @param mediaType the Jakarta Rest media type to check
     * @param typesToMatch the types to match (any one of them is considered a match)
     * @param subtypeToMatch the subtype to match
     * @return true if the type is any of the acceptable types and subtype matches, otherwise false
     */
    public static boolean matchesTypeAndSubtype(jakarta.ws.rs.core.MediaType mediaType,
                                                Set<String> typesToMatch,
                                                String subtypeToMatch) {
        checkJakartaMediaType(mediaType);
        return matchesTypeAndSubtype(mediaType.toString(), typesToMatch, subtypeToMatch);
    }

    private static void checkJakartaMediaType(jakarta.ws.rs.core.MediaType mediaType) {
        checkArgumentNotNull(mediaType, "mediaType must not be null");
    }

    /**
     * Checks if the given media type has a type and subtype that matches the given values.
     * <p>
     * This method lets you test for subtypes which can have more than one type.
     * For example, "application/xml" and "text/xml" are both considered valid XML types.
     *
     * @param mediaType the media type to check
     * @param typesToMatch the types to match (any one of them is considered a match)
     * @param subtypeToMatch the subtype to match
     * @return true if the type is any of the acceptable types and subtype matches, otherwise false
     */
    public static boolean matchesTypeAndSubtype(String mediaType, Set<String> typesToMatch, String subtypeToMatch) {
        checkMediaTypeNotBlank(mediaType);
        checkArgumentNotEmpty(typesToMatch, "typesToMatch must not be empty");

        var anyBlank = typesToMatch.stream().anyMatch(StringUtils::isBlank);
        checkArgument(!anyBlank, "typesToMatch must not contain blank elements");

        checkArgumentNotBlank(subtypeToMatch, "subtypeToMatch must not be blank");

        var parsedType = MediaType.parse(mediaType);
        var type = parsedType.type();
        var subtype = parsedType.subtype();
        return typesToMatch.contains(type) && subtypeToMatch.equals(subtype);
    }

    private static void checkMediaTypeNotBlank(String mediaType) {
        checkArgumentNotBlank(mediaType, "mediaType must not be blank");
    }
}
