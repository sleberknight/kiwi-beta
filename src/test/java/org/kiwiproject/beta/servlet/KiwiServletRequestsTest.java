package org.kiwiproject.beta.servlet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.beta.test.servlet.KiwiServletMocks;

import java.security.cert.X509Certificate;

@DisplayName("KiwiServletRequests")
class KiwiServletRequestsTest {

    @Nested
    class HasCertificates {

        @Test
        void shouldBeTrue_WhenRequestContainsACertificate() {
            var name = "CN=Bob,OU=Sales,O=ACME Inc,C=US";
            var request = KiwiServletMocks.mockHttpServletRequestWithCertificate(name);
            assertThat(KiwiServletRequests.hasCertificates(request)).isTrue();
        }

        @Test
        void shouldBeFalse_WhenRequestDoesNotContainACertificate() {
            var request = KiwiServletMocks.mockHttpServletRequestWithNoCertificate();
            assertThat(KiwiServletRequests.hasCertificates(request)).isFalse();
        }
    }

    @Nested
    class GetCertificates {

        @Test
        void shouldReturnTheCerts_WhenRequestContainsACertificate() {
            var name = "CN=Alice,OU=Marketing,O=ACME Inc,C=US";
            var request = KiwiServletMocks.mockHttpServletRequestWithCertificate(name);
            var certificates = KiwiServletRequests.getCertificates(request);

            assertThat(certificates)
                    .isNotNull()
                    .extracting(cert -> cert.getSubjectX500Principal().getName())
                    .containsExactly(name);
        }

        @Test
        void shouldReturnNull_WhenRequestDoesNotContainACertificate() {
            var request = KiwiServletMocks.mockHttpServletRequestWithNoCertificate();
            var certificates = KiwiServletRequests.getCertificates(request);
            assertThat(certificates).isNull();
        }
    }

    @Nested
    class GetCertificatesOrEmpty {

        @Test
        void shouldReturnTheCerts_WhenRequestContainsACertificate() {
            var name = "CN=Diane,OU=Engineering,O=ACME Inc,C=US";
            var request = KiwiServletMocks.mockHttpServletRequestWithCertificate(name);
            var certificates = KiwiServletRequests.getCertificatesOrEmpty(request);

            assertThat(certificates).isPresent();

            assertThat(certificates.get())
                    .extracting(cert -> cert.getSubjectX500Principal().getName())
                    .containsExactly(name);
        }

        @Test
        void shouldReturnNull_WhenRequestDoesNotContainACertificate() {
            var request = KiwiServletMocks.mockHttpServletRequestWithNoCertificate();
            var certificates = KiwiServletRequests.getCertificatesOrEmpty(request);
            assertThat(certificates).isEmpty();
        }
    }

    @Nested
    class GetCertificatesAsList {

        @Test
        void shouldReturnTheCerts_WhenRequestContainsACertificate() {
            var name = "CN=Alice,OU=Marketing,O=ACME Inc,C=US";
            var request = KiwiServletMocks.mockHttpServletRequestWithCertificate(name);
            var certificates = KiwiServletRequests.getCertificatesAsList(request);

            assertThat(certificates)
                    .isNotNull()
                    .extracting(cert -> cert.getSubjectX500Principal().getName())
                    .containsExactly(name);
        }

        @Test
        void shouldReturnEmptyList_WhenRequestDoesNotContainACertificate() {
            var request = KiwiServletMocks.mockHttpServletRequestWithNoCertificate();
            var certificates = KiwiServletRequests.getCertificatesAsList(request);
            assertThat(certificates).isEmpty();
        }
    }

    @Nested
    class HasCertificateIn {

        @Test
        void shouldBeTrue_WhenCertArrayContainsCert() {
            var cert = mock(X509Certificate.class);
            assertThat(KiwiServletRequests.hasCertificateIn(new X509Certificate[]{cert})).isTrue();
        }

        @Test
        void shouldBeFalse_WhenCertArrayIsNull() {
            assertThat(KiwiServletRequests.hasCertificateIn(null)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenFirstCertIsNull() {
            assertThat(KiwiServletRequests.hasCertificateIn(new X509Certificate[]{null})).isFalse();
        }
    }

    @Nested
    class DoesNotHaveCertificateIn {

        @Test
        void shouldBeTrue_WhenCertArrayIsNull() {
            assertThat(KiwiServletRequests.doesNotHaveCertificateIn(null)).isTrue();
        }

        @Test
        void shouldBeTrue_WhenFirstCertArrayIsNull() {
            assertThat(KiwiServletRequests.doesNotHaveCertificateIn(new X509Certificate[]{null})).isTrue();
        }

        @Test
        void shouldBeFalse_WhenCertArrayContainsCert() {
            var cert = mock(X509Certificate.class);
            assertThat(KiwiServletRequests.doesNotHaveCertificateIn(new X509Certificate[]{cert})).isFalse();
        }
    }

    @Nested
    class FirstCertificate {

        @Test
        void shouldReturnFirstCertificate() {
            var cert = mock(X509Certificate.class);
            var certs = new X509Certificate[]{cert};
            assertThat(KiwiServletRequests.firstCertificate(certs)).isSameAs(cert);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldThrowIllegalState_WhenGivenNullOrEmptyCertArray(X509Certificate[] certs) {
            assertThatIllegalStateException().isThrownBy(() -> KiwiServletRequests.firstCertificate(certs));
        }
    }

    @Nested
    class FirstCertificateOrEmpty {

        @Test
        void shouldReturnFirstCertificate() {
            var cert = mock(X509Certificate.class);
            var certs = new X509Certificate[]{cert};
            assertThat(KiwiServletRequests.firstCertificateOrEmpty(certs)).contains(cert);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldReturnEmpty_WhenGivenNullOrEmptyCertArray(X509Certificate[] certs) {
            assertThat(KiwiServletRequests.firstCertificateOrEmpty(certs)).isEmpty();
        }

        @Test
        void shouldReturnEmpty_WhenCertArrayContainsNullCert() {
            var certs = new X509Certificate[]{null};
            assertThat(KiwiServletRequests.firstCertificateOrEmpty(certs)).isEmpty();
        }
    }

}
