package org.kiwiproject.beta.base;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.annotations.Beta;
import com.google.common.base.CaseFormat;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Optional;

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
     * @return Optional containing the converted snake_case value, or an empty Optional
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
}
