package org.kiwiproject.beta.net;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.test.junit.jupiter.params.provider.MinimalBlankStringSource;

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

    @Nested
    class ResolveLocalAddressPreferringEnv {

        private static final String HOSTNAME_ENV_VAR = "CUSTOM_HOSTNAME";
        private static final String IP_ENV_VAR = "CUSTOM_IP";

        private KiwiEnvironment originalKiwiEnv;
        private KiwiEnvironment kiwiEnv;

        @BeforeEach
        void setUp() {
            originalKiwiEnv = KiwiInternetAddresses2.kiwiEnvironment;

            kiwiEnv = mock(KiwiEnvironment.class);
            KiwiInternetAddresses2.kiwiEnvironment = kiwiEnv;
        }

        @AfterEach
        void resetKiwiEnvironment() {
            KiwiInternetAddresses2.kiwiEnvironment = originalKiwiEnv;
        }

        @ParameterizedTest
        @MinimalBlankStringSource
        void shouldResolveToLocalhost_WhenHostnameEnvVar_IsBlank(String hostnameEnvVar) {
            when(kiwiEnv.getenv(anyString())).thenReturn("192.168.1.150");

            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringEnv(hostnameEnvVar, IP_ENV_VAR);

            assertThat(address.getHostname()).isEqualTo(localhost.getHostName());
            assertThat(address.getIp()).isEqualTo("192.168.1.150");

            verify(kiwiEnv).getenv(IP_ENV_VAR);
            verifyNoMoreInteractions(kiwiEnv);
        }

        @ParameterizedTest
        @MinimalBlankStringSource
        void shouldResolveToLocalAddress_WhenIpEnvVar_IsBlank(String ipEnvVar) {
            when(kiwiEnv.getenv(anyString())).thenReturn("test.host.acme.com");

            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringEnv(HOSTNAME_ENV_VAR, ipEnvVar);

            assertThat(address.getHostname()).isEqualTo("test.host.acme.com");
            assertThat(address.getIp()).isEqualTo(localhost.getHostAddress());

            verify(kiwiEnv).getenv(HOSTNAME_ENV_VAR);
            verifyNoMoreInteractions(kiwiEnv);
        }

        @ParameterizedTest
        @MinimalBlankStringSource
        void shouldAllowBlankHostnameAndIpEnvVars(String blankString) {
            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringEnv(blankString, blankString);

            assertThat(address.getHostname()).isEqualTo(localhost.getHostName());
            assertThat(address.getIp()).isEqualTo(localhost.getHostAddress());

            verifyNoInteractions(kiwiEnv);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldResolveToLocalHost_WhenHostnameFromEnv_IsBlank(String blankHostname) {
            when(kiwiEnv.getenv(HOSTNAME_ENV_VAR)).thenReturn(blankHostname);
            when(kiwiEnv.getenv(IP_ENV_VAR)).thenReturn("192.168.1.125");

            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringEnv(HOSTNAME_ENV_VAR, IP_ENV_VAR);

            assertThat(address.getHostname()).isEqualTo(localhost.getHostName());
            assertThat(address.getIp()).isEqualTo("192.168.1.125");

            verify(kiwiEnv).getenv(HOSTNAME_ENV_VAR);
            verify(kiwiEnv).getenv(IP_ENV_VAR);
            verifyNoMoreInteractions(kiwiEnv);
        }

        @ParameterizedTest
        @NullAndEmptySource
        void shouldResolveToLocalAddress_WhenIpFromEnv_IsBlank(String blankIp) {
            when(kiwiEnv.getenv(HOSTNAME_ENV_VAR)).thenReturn("test.acme.com");
            when(kiwiEnv.getenv(IP_ENV_VAR)).thenReturn(blankIp);

            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringEnv(HOSTNAME_ENV_VAR, IP_ENV_VAR);

            assertThat(address.getHostname()).isEqualTo("test.acme.com");
            assertThat(address.getIp()).isEqualTo(localhost.getHostAddress());

            verify(kiwiEnv).getenv(HOSTNAME_ENV_VAR);
            verify(kiwiEnv).getenv(IP_ENV_VAR);
            verifyNoMoreInteractions(kiwiEnv);
        }

        @Test
        void shouldResolveToEnvironmentVariables_WhenNotBlank() {
            when(kiwiEnv.getenv(HOSTNAME_ENV_VAR)).thenReturn("test.acme.com");
            when(kiwiEnv.getenv(IP_ENV_VAR)).thenReturn("192.168.1.142");

            var address = KiwiInternetAddresses2.resolveLocalAddressPreferringEnv(HOSTNAME_ENV_VAR, IP_ENV_VAR);

            assertThat(address.getHostname()).isEqualTo("test.acme.com");
            assertThat(address.getIp()).isEqualTo("192.168.1.142");

            verify(kiwiEnv).getenv(HOSTNAME_ENV_VAR);
            verify(kiwiEnv).getenv(IP_ENV_VAR);
            verifyNoMoreInteractions(kiwiEnv);
        }
    }
}
