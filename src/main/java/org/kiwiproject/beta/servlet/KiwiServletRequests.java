package org.kiwiproject.beta.servlet;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.kiwiproject.collect.KiwiArrays;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import java.security.cert.X509Certificate;
import java.util.Optional;

/**
 * Static utilities related to {@link ServletRequest}, mostly related to handling {@link X509Certificate}s.
 */
@UtilityClass
@Beta
public class KiwiServletRequests {

    public static final String X509_CERTIFICATE_ATTRIBUTE = "javax.servlet.request.X509Certificate";

    public static boolean hasCertificates(ServletRequest request) {
        return nonNull(request.getAttribute(X509_CERTIFICATE_ATTRIBUTE));
    }

    public static @Nullable
    X509Certificate[] getCertificates(ServletRequest request) {
        return (X509Certificate[]) request.getAttribute(X509_CERTIFICATE_ATTRIBUTE);
    }

    public static Optional<X509Certificate[]> getCertificatesOrEmpty(ServletRequest request) {
        var certificates = getCertificates(request);
        return Optional.ofNullable(certificates);
    }

    public static boolean hasCertificateIn(X509Certificate[] certificateChain) {
        return KiwiArrays.isNotNullOrEmpty(certificateChain) && nonNull(certificateChain[0]);
    }

    public static boolean doesNotHaveCertificateIn(X509Certificate[] certificateChain) {
        return KiwiArrays.isNullOrEmpty(certificateChain) || isNull(certificateChain[0]);
    }

    public static X509Certificate firstCertificate(X509Certificate[] certificateChain) {
        checkState(nonNull(certificateChain) && certificateChain.length > 0,
                "certificateChain must not be null and must have length > 0");
        return certificateChain[0];
    }

    public static Optional<X509Certificate> firstCertificateOrEmpty(X509Certificate[] certificateChain) {
        if (KiwiArrays.isNullOrEmpty(certificateChain)) {
            return Optional.empty();
        }

        return Optional.of(certificateChain[0]);
    }
}
