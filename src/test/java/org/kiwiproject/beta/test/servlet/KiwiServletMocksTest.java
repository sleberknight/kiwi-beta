package org.kiwiproject.beta.test.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.beta.servlet.KiwiServletRequests;

import java.security.cert.X509Certificate;

@DisplayName("KiwiServletMocks")
class KiwiServletMocksTest {

    @Test
    void shouldMockHttpServletRequestWithCertificate() {
        var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";

        var mockRequest = KiwiServletMocks.mockHttpServletRequestWithCertificate(dn);

        var certs = mockRequest.getAttribute(KiwiServletRequests.X509_CERTIFICATE_ATTRIBUTE);
        assertThat(certs).isInstanceOf(X509Certificate[].class);

        var x509Certs = (X509Certificate[]) certs;
        assertThat(x509Certs).hasSize(1);

        var x509Cert = x509Certs[0];

        //noinspection deprecation
        var principal = x509Cert.getSubjectDN();
        assertThat(principal.getName()).isEqualTo(dn);

        var x500Principal = x509Cert.getSubjectX500Principal();
        assertThat(x500Principal.getName()).isEqualToIgnoringWhitespace(dn);
    }

    @Test
    void shouldMockHttpServletRequestWithNoCertificate() {
        var mockRequest = KiwiServletMocks.mockHttpServletRequestWithNoCertificate();

        var certs = mockRequest.getAttribute(KiwiServletRequests.X509_CERTIFICATE_ATTRIBUTE);
        assertThat(certs).isNull();
    }

    @Nested
    class X509CertificateArgumentMatchers {

        @Test
        void shouldMatchExpectedCertArrayBySubjectDN() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesExpectedCertArrayBySubjectDN(dn);

            var cert = KiwiServletMocks.mockX509Certificate(dn);
            var certs = new X509Certificate[] {cert};
            assertThat(matcher.matches(certs)).isTrue();
        }

        @Test
        void shouldThrowWhenDoesNotMatchExpectedCertArrayBySubjectDN() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesExpectedCertArrayBySubjectDN(dn);

            var cert = KiwiServletMocks.mockX509Certificate("CN=Jane Doe, OU=Test, O=Kiwiproject, C=US");
            var certs = new X509Certificate[] {cert};

            assertThatThrownBy(() -> matcher.matches(certs))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void shouldMatchExpectedCertBySubjectDN() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesExpectedCertBySubjectDN(dn);

            var cert = KiwiServletMocks.mockX509Certificate(dn);
            assertThat(matcher.matches(cert)).isTrue();
        }

        @Test
        void shouldThrowWhenDoesNotMatchExpectedCertBySubjectDN() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesExpectedCertBySubjectDN(dn);

            var cert = KiwiServletMocks.mockX509Certificate("CN=Jane Doe, OU=Test, O=Kiwiproject, C=US");
            assertThatThrownBy(() -> matcher.matches(cert))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void shouldMatchExpectedCertArrayByX500PrincipalName() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesCertArrayByX500PrincipalName(dn);

            var cert = KiwiServletMocks.mockX509Certificate(dn);
            var certs = new X509Certificate[] {cert};
            assertThat(matcher.matches(certs)).isTrue();
        }

        @Test
        void shouldThrowWhenDoesNotMatchExpectedCertArrayByX500PrincipalName() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesCertArrayByX500PrincipalName(dn);

            var cert = KiwiServletMocks.mockX509Certificate("CN=Jane Doe, OU=Test, O=Kiwiproject, C=US");
            var certs = new X509Certificate[] {cert};

            assertThatThrownBy(() -> matcher.matches(certs))
                    .isInstanceOf(AssertionError.class);
        }

        @Test
        void shouldMatchExpectedCertByX500PrincipalName() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesExpectedCertByX500PrincipalName(dn);

            var cert = KiwiServletMocks.mockX509Certificate(dn);
            assertThat(matcher.matches(cert)).isTrue();
        }

        @Test
        void shouldThrowWhenDoesNotMatchExpectedCertByX500PrincipalName() {
            var dn = "CN=John Doe, OU=Test, O=Kiwiproject, C=US";
            var matcher = KiwiServletMocks.matchesExpectedCertByX500PrincipalName(dn);

            var cert = KiwiServletMocks.mockX509Certificate("CN=Jane Doe, OU=Test, O=Kiwiproject, C=US");
            assertThatThrownBy(() -> matcher.matches(cert))
                    .isInstanceOf(AssertionError.class);
        }

    }
}

