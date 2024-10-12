package org.kiwiproject.beta.base.jar;

/**
 * Represents the result of an attribute lookup.
 */
public enum AttributeLookupStatus {

    /**
     * The attribute exists.
     */
    EXISTS,

    /**
     * The attribute does not exist.
     */
    DOES_NOT_EXIST,

    /**
     * An I/O or other error occurred retrieving the attribute.
     */
    FAILURE
}
