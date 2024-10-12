package org.kiwiproject.beta.base.jar;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CheckReturnValue;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Utilities for working with JAR manifests.
 * <p>
 * Unlike {@link JarManifests}, this is an instance-based class, which allows
 * for easier testing using techniques such as mocking. In addition, some methods
 * in this class return "result" objects, which contain more information than
 * an Optional can convey.
 *
 * @see Manifest
 * @see JarManifests
 */
@Beta
public class JarManifestHelper {

    /**
     * Get the main attribute value in a Manifest, using the given Class to locate the
     * associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @param name     the name of the main attribute
     * @return a lookup result
     */
    @CheckReturnValue
    public AttributeLookupResult getMainAttributeValue(Class<?> theClass, String name) {
        try {
            var manifest = JarManifests.getManifestOrThrow(theClass);
            return getMainAttributeValue(manifest, name);
        } catch (Exception e) {
            return new AttributeLookupResult(AttributeLookupStatus.FAILURE, null, e);
        }
    }

    /**
     * Get the main attribute value in a Manifest having the given name.
     *
     * @param manifest the Manifest
     * @param name     the name of the main attribute
     * @return a lookup result
     */
    @CheckReturnValue
    public AttributeLookupResult getMainAttributeValue(Manifest manifest, String name) {
        var attributes = manifest.getMainAttributes();
        if (!attributes.containsKey(new Attributes.Name(name))) {
            return new AttributeLookupResult(AttributeLookupStatus.DOES_NOT_EXIST, null, null);
        }

        var value = attributes.getValue(name);
        return new AttributeLookupResult(AttributeLookupStatus.EXISTS, value, null);
    }

    /**
     * Return the main attributes of the Manifest, using the given Class to locate the
     * associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @return a lookup result
     */
    public AttributesLookupResult getMainAttributes(Class<?> theClass) {
        var lookupResult = getManifestWithResult(theClass);

        return switch (lookupResult.lookupStatus()) {
            case SUCCESS -> {
                var mainAttributes = getMainAttributes(lookupResult.manifestOrThrow());
                yield new AttributesLookupResult(AttributesLookupStatus.SUCCESS, mainAttributes, null);
            }
            case FAILURE -> new AttributesLookupResult(AttributesLookupStatus.FAILURE, null, lookupResult.error());
        };
    }

    /**
     * Return the main attributes of the given Manifest as a Map of String keys to String values.
     *
     * @param manifest the manifest
     * @return a map containing the main attributes
     */
    public Map<String, String> getMainAttributes(Manifest manifest) {
        return JarManifests.getMainAttributesAsMap(manifest);
    }

    /**
     * Use the given Class to locate the associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @return an Optional containing the Manifest, or an empty Optional if any error occurs
     */
    public Optional<Manifest> getManifest(Class<?> theClass) {
        return JarManifests.getManifest(theClass);
    }

    /**
     * Use the given Class to locate the associated {@code MANIFEST.MF} file.
     *
     * @param theClass the Class to use for finding the Manifest
     * @return a {@link ManifestLookupResult} containing information about the lookup
     */
    public ManifestLookupResult getManifestWithResult(Class<?> theClass) {
        var classHolder = new JarManifests.ClassHolder(theClass);
        return JarManifests.getManifest(classHolder);
    }

    /**
     * Use the given URI to locate the associated {@code MANIFEST.MF} file.
     *
     * @param jarFileURI the URI of the JAR file in which the manifest resides
     * @return an Optional containing the Manifest, or an empty Optional if any error occurs
     */
    public Optional<Manifest> getManifest(URI jarFileURI) {
        return JarManifests.getManifest(jarFileURI);
    }

    /**
     * Use the given URI to locate the associated {@code MANIFEST.MF} file.
     *
     * @param jarFileURI the URI of the JAR file in which the manifest resides
     * @return a {@link ManifestLookupResult} containing information about the lookup
     */
    public ManifestLookupResult getManifestWithResult(URI jarFileURI) {
        return JarManifests.getManifestWithResult(jarFileURI);
    }
}
