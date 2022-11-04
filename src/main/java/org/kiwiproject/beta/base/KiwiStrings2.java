package org.kiwiproject.beta.base;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.google.common.annotations.Beta;
import com.google.common.base.CaseFormat;

import org.checkerframework.checker.nullness.qual.Nullable;

import lombok.experimental.UtilityClass;

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

    public static String camelToSnakeCaseOrNull(@Nullable String camelCaseValue) {
        if (isBlank(camelCaseValue)) {
            return null;
        }

        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(camelCaseValue);
    }
}
