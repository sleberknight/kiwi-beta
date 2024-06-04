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
 *
 * @see Manifest
 */
@UtilityClass
@Beta
@Slf4j
public class JarManifests {

    public static String getMainAttributeValueOrThrow(Class<?> theClass, String name) {
        return getMainAttributeValue(theClass, name).orElseThrow(
                () -> new IllegalStateException(f("Unable to get value for main attribute {} for {}", name, theClass)));
    }

    public static Optional<String> getMainAttributeValue(Class<?> theClass, String name) {
        return getManifest(theClass)
                .map(manifest -> getMainAttributeValue(manifest, name))
                .flatMap(Function.identity());
    }

    public static Optional<String> getMainAttributeValue(Manifest manifest, String name) {
        var value = manifest.getMainAttributes().getValue(name);
        return Optional.ofNullable(value);
    }

    public static Map<String, String> getMainAttributesAsMapOrThrow(Class<?> theClass) {
        var manifest = getManifestOrThrow(theClass);
        return getMainAttributesAsMap(manifest);
    }

    public static Map<String, String> getMainAttributesAsMap(Manifest manifest) {
        return manifest.getMainAttributes()
                .entrySet()
                .stream()
                .collect(toUnmodifiableMap(e -> String.valueOf(e.getKey()), e -> String.valueOf(e.getValue())));
    }

    public static Manifest getManifestOrThrow(Class<?> theClass) {
        return getManifest(theClass)
                .orElseThrow(() -> new IllegalStateException("Unable to get manifest for " + theClass));
    }

    public static Optional<Manifest> getManifest(Class<?> theClass) {
        var classHolder = new ClassHolder(theClass);
        return getManifest(classHolder);
    }

    /**
     * @implNote Accepts a ClassHolder that can be mocked or overridden for testing purposes.
     * This is necessary to test all conditions, since Class is final and cannot be mocked.
     * ProtectionDomain is not final, but its getCodeSource() method is, so it also cannot
     * be mocked.
     */
    @VisibleForTesting
    static Optional<Manifest> getManifest(ClassHolder holder) {
        var theClass = holder.getContainedClass();
        try {
            var codeSource = holder.getProtectionDomain().getCodeSource();
            if (nonNull(codeSource)) {
                var location = codeSource.getLocation();
                LOG.trace("CodeSource location of {}: {}", theClass, location);
                if (nonNull(location)) {
                    return getManifest(location.toURI());
                }
            }

            LOG.warn("Unable to get manifest of JAR file for {}", theClass);
            return Optional.empty();
        } catch (Exception e) {
            LOG.error("Error getting manifest of JAR for {}", theClass, e);
            return Optional.empty();
        }
    }

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

    public static Optional<Manifest> getManifest(URI jarFileURI) {
        try (var jarFile = new JarFile(new File(jarFileURI))) {
            return Optional.of(jarFile.getManifest());
        } catch (IOException e) {
            LOG.error("Error getting manifest of JAR for URI {}", jarFileURI, e);
            return Optional.empty();
        }
    }
}
