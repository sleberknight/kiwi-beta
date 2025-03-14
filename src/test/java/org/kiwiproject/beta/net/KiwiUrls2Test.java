package org.kiwiproject.beta.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.net.KiwiUrls;

import java.net.URL;
import java.util.List;
import java.util.stream.Stream;

@DisplayName("KiwiUrls2")
class KiwiUrls2Test {

    @Nested
    class UniqueHostOnlyUrls {

        @Test
        void shouldRequireNonNullArg() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiUrls2.uniqueHostOnlyUrls(null));
        }

        @Test
        void shouldReturnEmptySet_WhenGivenEmptyCollection() {
            assertThat(KiwiUrls2.uniqueHostOnlyUrls(List.of())).isEmpty();
        }

        @Test
        void shouldReturnUniqueHosts() {
            var urls = Stream.of(
                            "https://dev-jump-proxy-1.acme.com/proxy/registry1/eureka",
                            "https://dev-jump-proxy-1.acme.com/proxy/registry2/eureka",
                            "https://dev-jump-proxy-2.acme.com/proxy/registry1/eureka",
                            "https://dev-jump-proxy-2.acme.com/proxy/registry2/eureka",
                            "https://dev-jump-proxy-1.acme.com/proxy/discovery",
                            "https://dev-jump-proxy-2.acme.com/proxy/discovery")
                    .map(KiwiUrls::createUrlObject)
                    .toList();

            var uniqueHostOnlyUrls = KiwiUrls2.uniqueHostOnlyUrls(urls);

            assertThat(uniqueHostOnlyUrls)
                    .extracting(URL::toString)
                    .containsExactlyInAnyOrder(
                            "https://dev-jump-proxy-1.acme.com",
                            "https://dev-jump-proxy-2.acme.com"
                    );
        }

        @Test
        void shouldReturnUniqueHosts_AndRetainPort() {
            var urls = Stream.of(
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/registry1/eureka",
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/registry2/eureka",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/registry1/eureka",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/registry2/eureka",
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/discovery",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/discovery")
                    .map(KiwiUrls::createUrlObject)
                    .toList();

            var uniqueHostOnlyUrls = KiwiUrls2.uniqueHostOnlyUrls(urls);

            assertThat(uniqueHostOnlyUrls)
                    .extracting(URL::toString)
                    .containsExactlyInAnyOrder(
                            "https://dev-jump-proxy-1.acme.com:7443",
                            "https://dev-jump-proxy-2.acme.com:7443"
                    );
        }
    }

    @Nested
    class UniqueAuthorityOnlyUrls {

        @Test
        void shouldRequireNonNullArg() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiUrls2.uniqueAuthorityOnlyUrls(null));
        }

        @Test
        void shouldReturnEmptySet_WhenGivenEmptyCollection() {
            assertThat(KiwiUrls2.uniqueAuthorityOnlyUrls(List.of())).isEmpty();
        }

        @Test
        void shouldReturnUniqueHosts() {
            var urls = Stream.of(
                            "https://dev-jump-proxy-1.acme.com/proxy/registry1/eureka",
                            "https://dev-jump-proxy-1.acme.com/proxy/registry2/eureka",
                            "https://dev-jump-proxy-2.acme.com/proxy/registry1/eureka",
                            "https://dev-jump-proxy-2.acme.com/proxy/registry2/eureka",
                            "https://dev-jump-proxy-1.acme.com/proxy/discovery",
                            "https://dev-jump-proxy-2.acme.com/proxy/discovery")
                    .map(KiwiUrls::createUrlObject)
                    .toList();

            var uniqueAuthorityOnlyUrls = KiwiUrls2.uniqueAuthorityOnlyUrls(urls);

            assertThat(uniqueAuthorityOnlyUrls)
                    .extracting(URL::toString)
                    .containsExactlyInAnyOrder(
                            "https://dev-jump-proxy-1.acme.com",
                            "https://dev-jump-proxy-2.acme.com"
                    );
        }

        @Test
        void shouldReturnUniqueHosts_AndRetainPort() {
            var urls = Stream.of(
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/registry1/eureka",
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/registry2/eureka",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/registry1/eureka",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/registry2/eureka",
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/discovery",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/discovery")
                    .map(KiwiUrls::createUrlObject)
                    .toList();

            var uniqueAuthorityOnlyUrls = KiwiUrls2.uniqueAuthorityOnlyUrls(urls);

            assertThat(uniqueAuthorityOnlyUrls)
                    .extracting(URL::toString)
                    .containsExactlyInAnyOrder(
                            "https://dev-jump-proxy-1.acme.com:7443",
                            "https://dev-jump-proxy-2.acme.com:7443"
                    );
        }
    }

    @Nested
    class UniqueAuthorityOnlyUrlsAsList {

        @Test
        void shouldRequireNonNullArg() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiUrls2.uniqueAuthorityOnlyUrlsAsList(null));
        }

        @Test
        void shouldReturnEmptySet_WhenGivenEmptyCollection() {
            assertThat(KiwiUrls2.uniqueAuthorityOnlyUrlsAsList(List.of())).isEmpty();
        }

        @Test
        void shouldReturnUniqueHosts() {
            var urls = Stream.of(
                            "https://dev-jump-proxy-1.acme.com/proxy/registry1/eureka",
                            "https://dev-jump-proxy-1.acme.com/proxy/registry2/eureka",
                            "https://dev-jump-proxy-2.acme.com/proxy/registry1/eureka",
                            "https://dev-jump-proxy-2.acme.com/proxy/registry2/eureka",
                            "https://dev-jump-proxy-1.acme.com/proxy/discovery",
                            "https://dev-jump-proxy-2.acme.com/proxy/discovery")
                    .map(KiwiUrls::createUrlObject)
                    .toList();

            var uniqueAuthorityOnlyUrls = KiwiUrls2.uniqueAuthorityOnlyUrlsAsList(urls);

            assertThat(uniqueAuthorityOnlyUrls)
                    .extracting(URL::toString)
                    .containsExactlyInAnyOrder(
                            "https://dev-jump-proxy-1.acme.com",
                            "https://dev-jump-proxy-2.acme.com"
                    );
        }

        @Test
        void shouldReturnUniqueHosts_AndRetainPort() {
            var urls = Stream.of(
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/registry1/eureka",
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/registry2/eureka",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/registry1/eureka",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/registry2/eureka",
                            "https://dev-jump-proxy-1.acme.com:7443/proxy/discovery",
                            "https://dev-jump-proxy-2.acme.com:7443/proxy/discovery")
                    .map(KiwiUrls::createUrlObject)
                    .toList();

            var uniqueAuthorityOnlyUrls = KiwiUrls2.uniqueAuthorityOnlyUrlsAsList(urls);

            assertThat(uniqueAuthorityOnlyUrls)
                    .extracting(URL::toString)
                    .containsExactlyInAnyOrder(
                            "https://dev-jump-proxy-1.acme.com:7443",
                            "https://dev-jump-proxy-2.acme.com:7443"
                    );
        }
    }

    @Nested
    class HostOnlyUrlFrom {

        @Test
        void shouldRequireNonNullArg() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiUrls2.hostOnlyUrlFrom(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https://dev-jump-proxy-1.acme.com/proxy/registry1/eureka",
                "https://dev-jump-proxy-1.acme.com/proxy/registry2/eureka",
                "https://dev-jump-proxy-1.acme.com/proxy/registry1/consul",
                "https://dev-jump-proxy-1.acme.com/proxy/discovery",
                "https://dev-jump-proxy-1.acme.com/proxy/registry1/eureka?param1=value1",
                "https://dev-jump-proxy-1.acme.com/proxy/discovery/param1=value1&param2=value2"
        })
        void shouldRemovePathsAndQueryStrings(String urlSpec) {
            var url = KiwiUrls.createUrlObject(urlSpec);
            var modifiedUri = KiwiUrls2.hostOnlyUrlFrom(url);
            assertThat(modifiedUri).hasToString("https://dev-jump-proxy-1.acme.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry1/eureka",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry2/eureka",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry1/consul",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/discovery",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry1/eureka?param1=value1",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/discovery/param1=value1&param2=value2"
        })
        void shouldRemovePathsAndQueryStrings_ButRetainPort(String urlSpec) {
            var url = KiwiUrls.createUrlObject(urlSpec);
            var modifiedUri = KiwiUrls2.hostOnlyUrlFrom(url);
            assertThat(modifiedUri).hasToString("https://dev-jump-proxy-1.acme.com:8443");
        }
    }

    @Nested
    class AuthorityOnlyUrlFrom {

        @Test
        void shouldRequireNonNullArg() {
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiUrls2.authorityOnlyUrlFrom(null));
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https://dev-jump-proxy-1.acme.com/proxy/registry1/eureka",
                "https://dev-jump-proxy-1.acme.com/proxy/registry2/eureka",
                "https://dev-jump-proxy-1.acme.com/proxy/registry1/consul",
                "https://dev-jump-proxy-1.acme.com/proxy/discovery",
                "https://dev-jump-proxy-1.acme.com/proxy/registry1/eureka?param1=value1",
                "https://dev-jump-proxy-1.acme.com/proxy/discovery/param1=value1&param2=value2"
        })
        void shouldRemoveQueryStrings(String urlSpec) {
            var url = KiwiUrls.createUrlObject(urlSpec);
            var modifiedUri = KiwiUrls2.authorityOnlyUrlFrom(url);
            assertThat(modifiedUri).hasToString("https://dev-jump-proxy-1.acme.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry1/eureka",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry2/eureka",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry1/consul",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/discovery",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/registry1/eureka?param1=value1",
                "https://dev-jump-proxy-1.acme.com:8443/proxy/discovery/param1=value1&param2=value2"
        })
        void shouldRemoveQueryStrings_AndRetainPort(String urlSpec) {
            var url = KiwiUrls.createUrlObject(urlSpec);
            var modifiedUri = KiwiUrls2.authorityOnlyUrlFrom(url);
            assertThat(modifiedUri).hasToString("https://dev-jump-proxy-1.acme.com:8443");
        }
    }
}
