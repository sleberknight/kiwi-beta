package org.kiwiproject.beta.base;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.annotations.Beta;
import com.google.common.base.CaseFormat;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utilities related to strings.
 * <p>
 * These utilities could be considered for kiwi's {@link org.kiwiproject.base.KiwiStrings} class.
 * Or somewhere else.
 * Or nowhere.
 */
@Beta
@UtilityClass
public class KiwiStrings2 {

    private static final Pattern NULL_CHAR_PATTERN = Pattern.compile("\u0000");

    /**
     * Convert a camelCase value to snake_case.
     *
     * @param value the camelCase value, must not be blank
     * @return the converted snake_case value
     * @throws IllegalArgumentException if value is blank
     */
    public static String camelToSnakeCase(String value) {
        return camelToSnakeCaseOrEmpty(value)
                .orElseThrow(() -> new IllegalArgumentException("value must not be blank"));
    }

    /**
     * Convert a camelCase value to snake_case.
     *
     * @param value the camelCase value
     * @return Optional containing the converted snake_case value, or an empty Optional if the input value is blank
     */
    public static Optional<String> camelToSnakeCaseOrEmpty(@Nullable String value) {
        return Optional.ofNullable(camelToSnakeCaseOrNull(value));
    }

    /**
     * Convert a camelCase value to snake_case.
     *
     * @param value the camelCase value
     * @return the converted snake_case value, or null if the input value is blank
     */
    public static String camelToSnakeCaseOrNull(@Nullable String value) {
        if (isBlank(value)) {
            return null;
        }

        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(value);
    }

    /**
     * Replace null characters (Unicode U+0000) in {@code str} with an empty string.
     *
     * @param str the string to replace within
     * @return a string with null characters replaced, or the original string if no null characters exist in it
     */
    public static String replaceNullCharactersWithEmpty(@Nullable String str) {
        return replaceNullCharacters(str, "", null);
    }

    /**
     * Replace null characters (Unicode U+0000) in {@code str} with the given replacement string. If the input
     * string is null, thne the default value is returned.
     *
     * @param str the string to replace within
     * @param replacement the replacement string
     * @param defaultValue the value to return if {@code str} is null
     * @return a string with null characters replaced, or the original string if no null characters exist in it
     */
    public static String replaceNullCharacters(@Nullable String str, String replacement, @Nullable String defaultValue) {
        return Optional.ofNullable(str)
                .map(s -> NULL_CHAR_PATTERN.matcher(s).replaceAll(replacement))
                .orElse(defaultValue);
    }
}
