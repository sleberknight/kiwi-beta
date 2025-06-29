package org.kiwiproject.beta.base.jar;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentIsNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * A record that contains a lookup status and, if the lookup succeeded, a value.
 *
 * @param lookupStatus the lookup status
 * @param value        the value, or null if the lookup did not succeed for any reason
 * @param error        the {@code Exception} that occurred during a failed lookup, or null if the cause was not an exception
 */
public record AttributeLookupResult(AttributeLookupStatus lookupStatus,
                                    @Nullable String value,
                                    @Nullable Exception error) {

    public AttributeLookupResult {
        if (lookupStatus == AttributeLookupStatus.EXISTS) {
            checkArgumentNotNull(value, "value must not be null when lookup succeeds");
            checkArgumentIsNull(error, "error must be null when lookup succeeds");
        } else {
            checkArgumentIsNull(value, "value must be null when lookup fails");
        }
    }

    /**
     * @return true if the lookup failed for any reason, otherwise true
     */
    public boolean failed() {
        return !succeeded();
    }

    /**
     * Check if the lookup succeeded; a successful lookup occurs only when
     * the attribute exists and has a value.
     *
     * @return true if the lookup succeeded, otherwise false
     */
    public boolean succeeded() {
        return lookupStatus == AttributeLookupStatus.EXISTS;
    }

    /**
     * @return true if, and only if, the value is not null
     */
    public boolean containsValue() {
        return nonNull(value);
    }

    /**
     * @return an Optional wrapping the value
     */
    public Optional<String> maybeValue() {
        return Optional.ofNullable(value);
    }

    /**
     * Return the attribute value if non-null. Otherwise, throw an {@link IllegalStateException}.
     *
     * @return the value if not-null
     * @throws IllegalStateException if value is null
     */
    public String valueOrThrow() {
        checkState(nonNull(value), "expected value not to be null");
        return value;
    }
}
