package org.kiwiproject.beta.base.jar;

/**
 * Represents the result of looking up all attributes.
 */
public enum AttributesLookupStatus {

    /**
     * The lookup returned attributes.
     */
    SUCCESS,

    /**
     * An I/O or other error occurred retrieving the attributes.
     */
    FAILURE
}
