package org.kiwiproject.beta.net;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import lombok.Value;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Supplier;

/**
 * Utilities related to internet addresses.
 * <p>
 * These utilities could be considered for kiwi's {@link org.kiwiproject.net.KiwiInternetAddresses} class.
 */
@Beta
@UtilityClass
public class KiwiInternetAddresses2 {

    // This is static and NOT final to facilitate testing
    @VisibleForTesting
    static KiwiEnvironment kiwiEnvironment = new DefaultEnvironment();

    /**
     * Return a {@link SimpleAddressHolder} containing a hostname and IP stored in the given
     * environment variables, or fall back to the hostname and address returned by
     * {@link InetAddress#getLocalHost()} when the environment variables do not exist or have
     * blank values.
     *
     * @param hostnameEnvVar the environment variable to check for hostname
     * @param ipEnvVar       the environment variable to check for IP address
     * @return a new SimpleAddressHolder instance
     * @see #resolveLocalAddressPreferringSupplied(Supplier, Supplier)
     */
    public static SimpleAddressHolder resolveLocalAddressPreferringEnv(@Nullable String hostnameEnvVar,
                                                                       @Nullable String ipEnvVar) {

        var hostnameFromEnv = isBlank(hostnameEnvVar) ? "" : kiwiEnvironment.getenv(hostnameEnvVar);
        var ipFromEnv = isBlank(ipEnvVar) ? "" : kiwiEnvironment.getenv(ipEnvVar);

        return resolveLocalAddressPreferringSupplied(() -> hostnameFromEnv, () -> ipFromEnv);
    }

    /**
     * Return a {@link SimpleAddressHolder} containing a hostname and IP supplied by the given
     * Suppliers, or falling back to the hostname and address returned by {@link InetAddress#getLocalHost()}
     * when a Supplier returns a blank String (null, empty string, whitespace only).
     * <p>
     * The primary use case for this is when you might want to override the default hostname and/or IP using
     * some external configuration such as environment variables or system properties specified via command line.
     * The suppliers can do something like: {@code () -> environment.getenv("CUSTOM_HOST"))}.
     *
     * @param hostnameSupplier supplier of hostname, the supplier may return blank string
     * @param ipSupplier       supplier of ip address, the supplier may return blank string
     * @return a new SimpleAddressHolder instance
     * @implNote This is a pretty narrow use case, so it most likely won't ever move into kiwi proper. The one
     * place we actually use this is in a "core service" library which does a bunch of setup and initialization
     * common across services. One of the specific things it does is to get the hostname and IP address to use
     * when registering a service, e.g., to Consul. We permit specific overrides to the hostname and/or IP address
     * via environment variables in a service host.
     */
    public static SimpleAddressHolder resolveLocalAddressPreferringSupplied(Supplier<String> hostnameSupplier,
                                                                            Supplier<String> ipSupplier) {

        checkArgumentNotNull(hostnameSupplier);
        checkArgumentNotNull(ipSupplier);

        var suppliedHostname = hostnameSupplier.get();
        var suppliedIp = ipSupplier.get();

        if (isBlank(suppliedHostname) || isBlank(suppliedIp)) {
            try {
                var localhost = InetAddress.getLocalHost();
                var hostname = isBlank(suppliedHostname) ? localhost.getHostName() : suppliedHostname;
                var ip = isBlank(suppliedIp) ? localhost.getHostAddress() : suppliedIp;
                return SimpleAddressHolder.of(hostname, ip);
            } catch (UnknownHostException e) {
                throw new IllegalStateException(
                        "Received an unexpected unknown host exception trying to get local host", e);
            }
        }

        return SimpleAddressHolder.of(suppliedHostname, suppliedIp);
    }

    @Value(staticConstructor = "of")
    public static class SimpleAddressHolder {
        String hostname;
        String ip;
    }
}
