package org.kiwiproject.beta.net;

import static java.util.stream.Collectors.toSet;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.kiwiproject.net.KiwiUrls;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

/**
 * Utilities related to URLs.
 * <p>
 * These utilities could be considered for kiwi's {@link org.kiwiproject.net.KiwiUrls} class.
 * Or, they could just stay here forever. One can never really know these things...
 */
@Beta
@UtilityClass
public class KiwiUrls2 {

    /**
     * Given a collection of URLs, return a set containing URLs with only the host[:port].
     * <p>
     * TODO Would this be better named as 'uniqueAuthorityOnlyUrls' or similar?
     */
    public static Set<URL> uniqueHostOnlyUrls(Collection<URL> urls) {
        checkArgumentNotNull(urls, "urls must not be null");
        return urls.stream()
                .map(KiwiUrls2::hostOnlyUrlFrom)
                .collect(toSet());
    }

    /*
        On usages of the above method, IntelliJ reports the "may contain URL objects" inspection, described as:

        Reports hashCode() and equals() calls on java.net.URL objects and calls that add URL objects to maps and sets.
        URL's equals() and hashCode() methods can perform a DNS lookup to resolve the host name. This may cause
        significant delays, depending on the availability and speed of the network and the DNS server. Using
        java.net.URI instead of java.net.URL will avoid the DNS lookup.

        TODO Consider changing these (or adding new methods) that accept URI objects
    */

    /**
     * Strip any path or query parameters from the given URL, returning only host[:port].
     */
    public static URL hostOnlyUrlFrom(URL url) {
        checkArgumentNotNull(url, "url must not be null");
        var urlSpec = url.getProtocol() + "://" + url.getAuthority();
        return KiwiUrls.createUrlObject(urlSpec);
    }
}
