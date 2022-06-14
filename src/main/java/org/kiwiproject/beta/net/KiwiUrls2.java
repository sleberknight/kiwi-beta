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

    /**
     * Strip any path or query parameters from the given URL, returning only host[:port].
     */
    public static URL hostOnlyUrlFrom(URL url) {
        checkArgumentNotNull(url, "url must not be null");
        var urlSpec = url.getProtocol() + "://" + url.getAuthority();
        return KiwiUrls.createUrlObject(urlSpec);
    }
}
