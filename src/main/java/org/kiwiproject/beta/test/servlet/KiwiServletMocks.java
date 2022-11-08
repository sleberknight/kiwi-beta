package org.kiwiproject.beta.test.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;
import org.kiwiproject.beta.servlet.KiwiServletRequests;
import org.mockito.ArgumentMatcher;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.security.cert.X509Certificate;

/**
 * Static utilities to create Mockito-based mocks for servlet API code.
 */
@UtilityClass
@Beta
public class KiwiServletMocks {

    /**
     * @implNote Don't inline the 'certificate' in the thenReturn() call on the mock request.
     * For some reason that I have not fully investigated, Mockito gets really upset and throws
     * a {@link org.mockito.exceptions.misusing.UnfinishedStubbingException}.
     */
    public static HttpServletRequest mockHttpServletRequestWithCertificate(String dn) {
        var request = mock(HttpServletRequest.class);
        var certificate = mockX509Certificate(dn);
        when(request.getAttribute(KiwiServletRequests.X509_CERTIFICATE_ATTRIBUTE))
                .thenReturn(new X509Certificate[]{certificate});
        return request;
    }

    /**
     * @implNote This is not strictly necessary since a Mockito mock will return null for methods that
     * return a reference type if not provided any expectations. But, it makes test code more explicit
     * about the intent of the code, so that's why this exists.
     */
    public static HttpServletRequest mockHttpServletRequestWithNoCertificate() {
        var request = mock(HttpServletRequest.class);
        when(request.getAttribute(KiwiServletRequests.X509_CERTIFICATE_ATTRIBUTE)).thenReturn(null);
        return request;
    }

    /**
     * @implNote Has to mock the {@link Principal} returned by {@link X509Certificate#getSubjectDN()} so
     * this actually creates two mocks. Also, since {@link X509Certificate#getSubjectX500Principal()} returns
     * an instance of the <em>final</em> class {@link X500Principal}, we can't mock it and instead a "real"
     * instance of {@link X500Principal} having the given distinguished name is returned. Also see
     * <a href="https://softwareengineering.stackexchange.com/questions/173396/deprecated-vs-denigrated-in-javadoc">StackOverflow entry</a>
     * regarding the getSubjectDN method being "denigrated". And, Java 16 deprecated {@link X509Certificate#getSubjectDN()}
     * though (as of this writing on Nov. 8, 2022) not for removal.
     */
    public static X509Certificate mockX509Certificate(String dn) {
        var cert = mock(X509Certificate.class);
        var principal = mock(Principal.class);

        when(cert.getSubjectDN()).thenReturn(principal);
        when(principal.getName()).thenReturn(dn);

        var x500Principal = new X500Principal(dn);
        when(cert.getSubjectX500Principal()).thenReturn(x500Principal);

        return cert;
    }

    /**
     * Argument matcher that matches a certificate array containing exactly one {@link X509Certificate}
     * having the given subject DN. Uses the {@link X509Certificate#getSubjectDN()} to obtain the
     * {@link Principal} and then matches against {@link Principal#getName()}.
     */
    public static ArgumentMatcher<X509Certificate[]> matchesExpectedCertArrayBySubjectDN(String subjectDn) {

        return certs -> {
            assertThat(certs)
                    .extracting(cert -> cert.getSubjectDN().getName())
                    .containsExactly(subjectDn);

            return true;
        };
    }

    /**
     * Argument matcher that matches a certificate having the given subject DN.
     */
    public static ArgumentMatcher<X509Certificate> matchesExpectedCertBySubjectDN(String subjectDn) {

        return cert -> {
            assertThat(cert.getSubjectDN().getName()).isEqualTo(subjectDn);

            return true;
        };
    }

    /**
     * Argument matcher that matches a certificate array containing exactly one {@link X509Certificate}
     * having an {@link X500Principal} with the given name. Uses {@link X509Certificate#getSubjectX500Principal()}
     * and to obtain the {@link X500Principal} and then matches against {@link X500Principal#getName()}.
     */
    public static ArgumentMatcher<X509Certificate[]> matchesCertArrayByX500PrincipalName(String name) {

        return object -> {
            assertThat(object).isInstanceOf(X509Certificate[].class);

            // Create an X500Principal to compare against, so that differences in whitespace are ignored.
            // X500Principal removes whitespace between components such that "CN=John Doe, OU=Test Org"
            // becomes "CN=John Doe,OU=Test Org".
            var x500Principal = new X500Principal(name);
            assertThat(object)
                    .extracting(cert -> cert.getSubjectX500Principal().getName())
                    .containsExactly(x500Principal.getName());

            return true;
        };
    }

    /**
     * Argument matcher that matches a certificate having an {@link X500Principal} with the given name.
     */
    public static ArgumentMatcher<X509Certificate> matchesExpectedCertByX500PrincipalName(String name) {

        return cert -> {
            // Create an X500Principal to compare against, so that differences in whitespace are ignored.
            // X500Principal removes whitespace between components such that "CN=John Doe, OU=Test Org"
            // becomes "CN=John Doe,OU=Test Org".
            var x500Principal = new X500Principal(name);
            assertThat(cert.getSubjectX500Principal().getName()).isEqualTo(x500Principal.getName());

            return true;
        };
    }
}
