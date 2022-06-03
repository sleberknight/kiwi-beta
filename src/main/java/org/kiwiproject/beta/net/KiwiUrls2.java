package org.kiwiproject.beta.net;

import static java.util.stream.Collectors.toSet;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

import com.google.common.annotations.Beta;

import org.kiwiproject.net.KiwiUrls;

import lombok.experimental.UtilityClass;

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
     * Given a collection of URIs, return a set containing URIs with only the host[:port].
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
