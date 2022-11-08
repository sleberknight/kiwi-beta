package org.kiwiproject.beta.test.servlet;

import static org.assertj.core.api.Assertions.assertThat;

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

        // TODO...
    }
}

