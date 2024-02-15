package org.kiwiproject.beta.net;

import static java.util.stream.Collectors.toSet;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.kiwiproject.net.KiwiUrls;

import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Utilities related to URLs and URIs.
 * <p>
 * These utilities could be considered for kiwi's {@link org.kiwiproject.net.KiwiUrls} class.
 * Or, they could just stay here forever. One can never really know these things...
 */
@Beta
@UtilityClass
public class KiwiUrls2 {

    /**
     * Given a collection of URLs, return a set containing URLs with only the host[:port] (the authority part).
     * <p>
     * This is an alias of {@link #uniqueAuthorityOnlyUrls(Collection)}.
     * <p>
     * <strong>Note:</strong> This method may perform DNS lookups when adding URL objects to the resulting Set.
     * This is because "URL's equals() and hashCode() methods can perform a DNS lookup to resolve the host name"
     * according to IntelliJ's  "may contain URL objects" inspection. If you need to avoid this, use the
     * {@link #uniqueAuthorityOnlyUrlsAsList(Collection)} method to get a List of unique URLs. However, note
     * that any subsequent conversion to a Set will potentially incur the same DNS lookups.
     *
     * @param urls the URLs to process
     * @return a set containing unique URLs containing only the host[:port] (the authority part) from the original URLs
     * @see #uniqueAuthorityOnlyUrlsAsList(Collection)
     */
    public static Set<URL> uniqueHostOnlyUrls(Collection<URL> urls) {
        return uniqueAuthorityOnlyUrls(urls);
    }

    /**
     * Given a collection of URLs, return a set containing URLs with only the authority ( host[:port] ).
     * <p>
     * <strong>Note:</strong> This method may perform DNS lookups when adding URL objects to the resulting Set.
     * This is because "URL's equals() and hashCode() methods can perform a DNS lookup to resolve the host name"
     * according to IntelliJ's  "may contain URL objects" inspection. If you need to avoid this, use the
     * {@link #uniqueAuthorityOnlyUrlsAsList(Collection)} method to get a List of unique URLs. However, note
     * that any subsequent conversion to a Set will potentially incur the same DNS lookups.
     *
     * @param urls the URLs to process
     * @return a set containing unique URLs containing only the authority part ( host[:port] ) from the original URLs
     * @see #uniqueAuthorityOnlyUrlsAsList(Collection)
     */
    public static Set<URL> uniqueAuthorityOnlyUrls(Collection<URL> urls) {
        checkArgumentNotNull(urls, "urls must not be null");

        // see explanation below this method on DNS lookups when adding URL objects to maps and sets

        return urls.stream()
                .map(KiwiUrls2::hostOnlyUrlFrom)
                .collect(toSet());
    }

    /*
        Background on DNS lookups performing when adding URL objects to maps and sets:

        On usages of the above methods, IntelliJ reports the "may contain URL objects" inspection, described as:

        Reports hashCode() and equals() calls on java.net.URL objects and calls that add URL objects to maps and sets.
        URL's equals() and hashCode() methods can perform a DNS lookup to resolve the host name. This may cause
        significant delays, depending on the availability and speed of the network and the DNS server. Using
        java.net.URI instead of java.net.URL will avoid the DNS lookup.

        This is the reason for the warnings in the javadocs, and also why the method that returns a list
        of unique authority-only URLs was added.
    */

    /**
     * Given a collection of URLs, return a list containing unique URLs with only the authority ( host[:port] ).
     * <p>
     * <strong>Note:</strong> This method avoids DNS lookups performed by URL equals() and hashCode() methods
     * that may occur when adding URL objects to sets and maps. Note also that any subsequent conversion to a
     * Set will potentially incur the same DNS lookups, so it is best to work directly with the list of URLs
     * returned by this method.
     *
     * @param urls the URLs to process
     * @return a list containing unique URLs containing only the authority part ( host[:port] ) from the original URLs
     */
    public static List<URL> uniqueAuthorityOnlyUrlsAsList(Collection<URL> urls) {
        checkArgumentNotNull(urls, "urls must not be null");

        // Avoid URL#equals and #hashCode methods by doing the distinct operation on
        // the authority-only URLs as Strings.

        return urls.stream()
                .map(KiwiUrls2::hostOnlyUrlFrom)
                .map(URL::toString)
                .distinct()
                .map(KiwiUrls::createUrlObject)
                .toList();
    }

    /**
     * Strip any path or query parameters from the given URL, returning only host[:port] (the authority part).
     * <p>
     * This is an alias of {@link #authorityOnlyUrlFrom(URL)}.
     *
     * @param url the URL to process
     * @return a URL containing only the host[:port] (the authority part) of the original URL
     * @see #authorityOnlyUrlFrom(URL)
     */
    public static URL hostOnlyUrlFrom(URL url) {
        return authorityOnlyUrlFrom(url);
    }

    /**
     * Strip any path or query parameters from the given URL, returning only the authority ( host[:port] ).
     *
     * @param url the URL to process
     * @return a URL containing only the authority part of the original URL
     */
    public static URL authorityOnlyUrlFrom(URL url) {
        checkArgumentNotNull(url, "url must not be null");
        var urlSpec = url.getProtocol() + "://" + url.getAuthority();
        return KiwiUrls.createUrlObject(urlSpec);
    }
}
