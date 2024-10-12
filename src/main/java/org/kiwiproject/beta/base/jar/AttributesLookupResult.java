package org.kiwiproject.beta.base.jar;

import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentIsNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * A record that contains a lookup status and, if the lookup succeeded, a map of all the attributes.
 *
 * @param lookupStatus the lookup status
 * @param attributes   a map containing the attributes, or null if the lookup failed for any reason
 * @param error        the Exception that occurred during a failed lookup, or null if the cause was not an exception
 */
public record AttributesLookupResult(AttributesLookupStatus lookupStatus,
                                     @Nullable Map<String, String> attributes,
                                     @Nullable Exception error) {

    public AttributesLookupResult {
        if (lookupStatus == AttributesLookupStatus.SUCCESS) {
            checkArgumentNotNull(attributes, "attributes must not be null when lookup succeeds");
            checkArgumentIsNull(error, "error must be null when lookup succeeds");
        } else {
            checkArgumentIsNull(attributes, "attributes must be null when lookup fails");
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
        return lookupStatus == AttributesLookupStatus.SUCCESS;
    }

    /**
     * @return true if, and only if, attributes is not null
     */
    public boolean hasAttributes() {
        return nonNull(attributes);
    }

    /**
     * @return an Optional wrapping the attributes
     */
    public Optional<Map<String, String>> maybeAttributes() {
        return Optional.ofNullable(attributes);
    }
}
