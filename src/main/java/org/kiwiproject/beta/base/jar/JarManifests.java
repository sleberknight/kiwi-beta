package org.kiwiproject.beta.base.jar;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.kiwiproject.base.KiwiStrings.f;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import lombok.AllArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Static utilities related to JAR manifests.
 * <p>
 * If you need to be able to mock the functionality in this class
 * for testing, consider using {@link JarManifestHelper} instead.
 *
 * @see Manifest
 * @see JarManifestHelper
 */
@UtilityClass
@Beta
@Slf4j
public class JarManifests {

    /**
     * Get the main attribute value in a Manifest, using the given Class to locate the
     * associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @param name     the name of the main attribute
     * @return the value of the attribute
     * @throws IllegalStateException if the lookup failed or the attribute does not exist
     */
    public static String getMainAttributeValueOrThrow(Class<?> theClass, String name) {
        return getMainAttributeValue(theClass, name).orElseThrow(
                () -> new IllegalStateException(f("Unable to get value for main attribute {} for {}", name, theClass)));
    }

    /**
     * Get the main attribute value in a Manifest, using the given Class to locate the
     * associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @param name     the name of the main attribute
     * @return an Optional containing the value, or an empty Optional if the looked failed
     */
    public static Optional<String> getMainAttributeValue(Class<?> theClass, String name) {
        return getManifest(theClass)
                .map(manifest -> getMainAttributeValue(manifest, name))
                .flatMap(Function.identity());
    }

    /**
     * Get the main attribute value in a Manifest having the given name.
     *
     * @param manifest the Manifest
     * @param name     the name of the main attribute
     * @return an Optional containing the value, or an empty Optional if the lookup failed
     */
    public static Optional<String> getMainAttributeValue(Manifest manifest, String name) {
        var value = manifest.getMainAttributes().getValue(name);
        return Optional.ofNullable(value);
    }

    /**
     * Return the main attributes of the Manifest, using the given Class to locate the
     * associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @return a map containing the main attributes
     * @throws IllegalStateException if the lookup fails for any reason
     */
    public static Map<String, String> getMainAttributesAsMapOrThrow(Class<?> theClass) {
        var manifest = getManifestOrThrow(theClass);
        return getMainAttributesAsMap(manifest);
    }

    /**
     * Return the main attributes of the Manifest, using the given Class to locate the
     * associated {@code MANIFEST.MF} file.
     *
     * @param manifest the manifest
     * @return a map containing the main attributes
     */
    public static Map<String, String> getMainAttributesAsMap(Manifest manifest) {
        return manifest.getMainAttributes()
                .entrySet()
                .stream()
                .collect(toUnmodifiableMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));
    }

    /**
     * Use the given Class to locate the associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @return the Manifest
     * @throws IllegalStateException if the lookup fails for any reason
     */
    public static Manifest getManifestOrThrow(Class<?> theClass) {
        var classHolder = new ClassHolder(theClass);
        var lookupResult = getManifest(classHolder);

        return switch (lookupResult.lookupStatus()) {
            case SUCCESS -> lookupResult.manifestOrThrow();
            case FAILURE -> throw illegalStateExceptionFor(lookupResult, theClass);
        };
    }

    private static IllegalStateException illegalStateExceptionFor(ManifestLookupResult lookupResult,
                                                                  Class<?> theClass) {

        var errorMessage = "Unable to get manifest for " + theClass;
        return Optional.ofNullable(lookupResult.error())
                .map(error -> new IllegalStateException(errorMessage, error))
                .orElseGet(() -> new IllegalStateException(errorMessage));
    }

    /**
     * Use the given Class to locate the associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @return an Optional containing the Manifest, or an empty Optional if the lookup failed
     */
    public static Optional<Manifest> getManifest(Class<?> theClass) {
        var classHolder = new ClassHolder(theClass);
        var lookupResult = getManifest(classHolder);
        return lookupResult.maybeManifest();
    }

    /**
     * @implNote Accepts a ClassHolder that can be mocked or overridden for testing purposes.
     * This is necessary to test all conditions, since Class is final and cannot be mocked.
     * ProtectionDomain is not final, but its getCodeSource() method is, so it also cannot
     * be mocked.
     */
    static ManifestLookupResult getManifest(ClassHolder holder) {
        var theClass = holder.getContainedClass();
        try {
            String errorMessage;
            var codeSource = holder.getProtectionDomain().getCodeSource();
            if (nonNull(codeSource)) {
                var location = codeSource.getLocation();
                LOG.trace("CodeSource location of {}: {}", theClass, location);
                if (nonNull(location)) {
                    return getManifestWithResult(location.toURI());
                } else {
                    errorMessage = f("The Location of the CodeSource was null. CodeSource: {}", codeSource);
                }
            } else {
                errorMessage = "The CodeSource from the ProtectionDomain was null";
            }

            LOG.warn("Unable to get manifest of JAR file for {}, cause: {}", theClass, errorMessage);
            return new ManifestLookupResult(ManifestLookupStatus.FAILURE, null, null, errorMessage);
        } catch (Exception e) {
            LOG.error("Error getting manifest of JAR for {}", theClass, e);
            return new ManifestLookupResult(ManifestLookupStatus.FAILURE, null, e, null);
        }
    }

    /**
     * This class is entirely to permit testing error conditions which
     * cannot otherwise be easily tested using the JDK classes directly.
     * <p>
     * For example, to allow simulating a {@link SecurityException} thrown
     * when calling {@link Class#getProtectionDomain()}.
     */
    @VisibleForTesting
    @AllArgsConstructor
    static class ClassHolder {
        private final Class<?> theClass;

        Class<?> getContainedClass() {
            return theClass;
        }

        ProtectionDomain getProtectionDomain() {
            return theClass.getProtectionDomain();
        }
    }

    /**
     * Use the given URI to locate the associated {@code MANIFEST.MF} file.
     *
     * @param jarFileURI the URI of the JAR file in which the manifest resides
     * @return an Optional containing the Manifest, or an empty Optional if any error occurs
     */
    public static Optional<Manifest> getManifest(URI jarFileURI) {
        var lookupResult = getManifestWithResult(jarFileURI);
        return lookupResult.maybeManifest();
    }

    static ManifestLookupResult getManifestWithResult(URI jarFileURI) {
        try (var jarFile = new JarFile(new File(jarFileURI))) {
            var manifest = jarFile.getManifest();
            return new ManifestLookupResult(ManifestLookupStatus.SUCCESS, manifest, null, null);
        } catch (IOException e) {
            LOG.error("Error getting manifest of JAR for URI {}", jarFileURI, e);
            return new ManifestLookupResult(ManifestLookupStatus.FAILURE, null, e, null);
        }
    }
}
