package org.kiwiproject.beta.dao;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.annotations.Beta;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Extremely simply way to whitelist fields, e.g. from a web form.
 */
@Beta
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AllowedFields {
    
    private final Set<String> fieldNames;
    private final Map<String, String> fieldNamesToPrefixedNames;

    /**
     * Create a new instance from the given field names.
     * <p>
     * Permits simple or prefixed field names in the format [prefix.optionally.with.multiple.parts.]fieldName.
     * If the given name is dot-separated, then the field name is extracted as the part after the last dot, while
     * the part before the last dot is considered the prefix.
     * <p>
     * Examples: firstName, u.firstName, user.firstName, account.user.firstName
     * 
     * @param fieldNames the field names to allow
     * @return a new instance
     */
    public static AllowedFields of(String... fieldNames) {
        checkArgumentNotNull(fieldNames, "fieldNames must not be null");
        
        return AllowedFields.of(Arrays.asList(fieldNames));
    }

    /**
     * Create a new instance from the collection of field names.
     * <p>
     * Permits simple or prefixed field names in the format [prefix.optionally.with.multiple.parts.]fieldName.
     * If the given name is dot-separated, then the field name is extracted as the part after the last dot, while
     * the part before the last dot is considered the prefix.
     * <p>
     * Examples: firstName, u.firstName, user.firstName, account.user.firstName
     * 
     * @param fieldNames the field names to allow
     * @return a new instance
     * @see #of(String...)
     */
    public static AllowedFields of(Collection<String> fieldNames) {
        checkArgumentNotNull(fieldNames, "fieldNames must not be null");
        checkArgument(!fieldNames.isEmpty(), "at least one field name must be specified");

        var fieldNameToPrefixedNameMap = fieldNames.stream()
            .map(PrefixAndFieldName::new)
            .collect(toUnmodifiableMap(
                prefixAndName -> prefixAndName.fieldName, 
                prefixAndName -> prefixAndName.prefixedFieldName));

        return new AllowedFields(Set.copyOf(fieldNameToPrefixedNameMap.keySet()), fieldNameToPrefixedNameMap);
    }

    private static class PrefixAndFieldName {
        final String fieldName;
        final String prefixedFieldName;

        PrefixAndFieldName(String name) {
            checkArgumentNotBlank(name, "field name must not be blank");

            var lastDot = name.lastIndexOf('.');

            if (lastDot == -1) {
                fieldName = name;
                prefixedFieldName = name;
            } else if (lastDot == 0 || lastDot == name.length() - 1) {  // dot at beginning or end
                throw new IllegalArgumentException("field name must be simple (firstName) or compound (user.firstName, account.user.firstName; cannot being or end with '.' (dot)");
            } else {
                fieldName = name.substring(lastDot + 1);
                prefixedFieldName = name;
            }
        }
    }

    /** 
     * @param fieldName field name to check
     * @return true if the given field name is allowed, false otherwise
     */
    public boolean isAllowed(String fieldName) {
        checkArgumentNotBlank(fieldName);
        return fieldNames.contains(fieldName);
    }

    /**
     * @param prefixedFieldName the prefixed field name
     * @return true if the given prefixe field name is allowed, false otherwise
     */
    public boolean isPrefixedAllowed(String prefixedFieldName) {
        checkArgumentNotBlank(prefixedFieldName);
        return fieldNamesToPrefixedNames.containsValue(prefixedFieldName);
    }

    /**
     * @param fieldName the field name to check
     * @throws IllegalArgumentException if the given field name is not allowed
     */
    public void assertAllowed(String fieldName) {
        checkArgumentNotBlank(fieldName);
        if (!isAllowed(fieldName)) {
            throw new IllegalArgumentException(fieldName + " is not allowed");
        }
    }

    /**
     * @param prefixedFieldName the prefixed field name to check
     * @throws IllegalArgumentException if the given prefixed field name is not allowed
     */
    public void assertPrefixedAllowed(String prefixedFieldName) {
        checkArgumentNotBlank(prefixedFieldName);
        if (!isPrefixedAllowed(prefixedFieldName)) {
            throw new IllegalArgumentException(prefixedFieldName + " is not allowed");
        }
    }

    /**
     * @param fieldName the field name to find
     * @return the full prefixe field name (e.g. u.lastName) for the given field name (e.g. lastName)
     */
    public String getPrefixedFieldName(String fieldName) {
        checkArgumentNotBlank(fieldName);
        return fieldNamesToPrefixedNames.get(fieldName);
    }
}
