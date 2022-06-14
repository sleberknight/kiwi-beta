package org.kiwiproject.beta.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;

@DisplayName("KiwiInternetAddresses2")
class KiwiInternetAddresses2Test {

    private Supplier<String> blankStringSupplier;
    private InetAddress localhost;

    @BeforeEach
    void setUp() throws UnknownHostException {
        blankStringSupplier = () -> " ";
        localhost = InetAddress.getLocalHost();
    }

    @Nested
    class ResolveLocalAddressPreferringSupplied {

        @Test
        void shouldRequireHostnameSupplier() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> KiwiInternetAddresses2.resolveLocalAddressPreferringSupplied(null, blankStringSupplier));
        }

        @Test
        void shouldRequireIpSupplier() {
            assertThatIllegalArgumentException().isThrownBy(
                    () -> KiwiInternetAddresses2.resolveLocalAddressPreferringSupplied(blankStringSupplier, null));
        }

        @Test
        void shouldUseSuppliedHostname() {
            var customHostname = "server1.acme.com";
            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringSupplied(() -> customHostname, blankStringSupplier);

            assertThat(address.getHostname()).isEqualTo(customHostname);
            assertThat(address.getIp()).isEqualTo(localhost.getHostAddress());
        }

        @Test
        void shouldUseSuppliedIp() {
            var customIp = "192.169.100.101";
            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringSupplied(blankStringSupplier, () -> customIp);

            assertThat(address.getHostname()).isEqualTo(localhost.getHostName());
            assertThat(address.getIp()).isEqualTo(customIp);
        }

        @Test
        void shouldUseSuppliedHostnameAndIp() {
            var customHostname = "server1.acme.com";
            var customIp = "192.169.100.101";
            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringSupplied(
                    () -> customHostname,
                    () -> customIp);

            assertThat(address.getHostname()).isEqualTo(customHostname);
            assertThat(address.getIp()).isEqualTo(customIp);
        }

        @Test
        void shouldUseLocalhostWhenSuppliersReturnBlank() {
            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringSupplied(blankStringSupplier, blankStringSupplier);

            assertThat(address.getHostname()).isEqualTo(localhost.getHostName());
            assertThat(address.getIp()).isEqualTo(localhost.getHostAddress());
        }
    }
}
