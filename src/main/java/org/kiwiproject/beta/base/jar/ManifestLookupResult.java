package org.kiwiproject.beta.base.jar;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentIsNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.jar.Manifest;

/**
 * A record that contains lookup status and, if the lookup succeeded, a {@link Manifest}.
 *
 * @param lookupStatus the lookup status
 * @param manifest     the Manifest, or null if the lookup failed for any reason
 * @param error        the Exception that occurred during a failed lookup, or null if the cause was not an exception
 * @param errorMessage an error message describing the cause of the lookup failure
 */
public record ManifestLookupResult(ManifestLookupStatus lookupStatus,
                                   @Nullable Manifest manifest,
                                   @Nullable Exception error,
                                   @Nullable String errorMessage) {

    public ManifestLookupResult {
        if (lookupStatus == ManifestLookupStatus.SUCCESS) {
            checkArgumentNotNull(manifest, "manifest must not be null when lookup succeeds");
            checkArgumentIsNull(error, "error must be null when lookup succeeds");
            checkArgumentIsNull(errorMessage, "error must be null when lookup succeeds");
        } else {
            checkArgumentIsNull(manifest, "manifest must be null when lookup fails");
        }
    }

    /**
     * @return true if the lookup failed for any reason, otherwise true
     */
    public boolean failed() {
        return !succeeded();
    }

    /**
     * @return true if the lookup succeeded, otherwise false
     */
    public boolean succeeded() {
        return lookupStatus == ManifestLookupStatus.SUCCESS;
    }

    /**
     * @return an Optional wrapping the manifest
     */
    public Optional<Manifest> maybeManifest() {
        return Optional.ofNullable(manifest);
    }

    /**
     * Return the Manifest if non-null. Otherwise, throw an {@link IllegalStateException}.
     *
     * @return the manifest if not-null
     * @throws IllegalStateException if the Manifest is null
     */
    public Manifest manifestOrThrow() {
        checkState(nonNull(manifest), "expected manifest not to be null");
        return manifest;
    }
}
