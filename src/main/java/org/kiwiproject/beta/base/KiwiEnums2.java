package org.kiwiproject.beta.base;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Static utilities for working with {@link Enum}.
 * <p>
 * These may eventually be moved into {@link org.kiwiproject.base.KiwiEnums}
 * in <a href="https://github.com/kiwiproject/kiwi">kiwi</a>.
 */
@UtilityClass
@Beta
public class KiwiEnums2 {

    /**
     * Return the constants in the given enum class as a list.
     * <p>
     * Kiwi version <a href="https://github.com/kiwiproject/kiwi/releases/tag/v4.12.0">4.12.0</a> adds
     * methods <a href="https://javadoc.io/static/org.kiwiproject/kiwi/4.12.0/org/kiwiproject/base/KiwiEnums.html#listOf(java.lang.Class)">listOf</a>
     * and <a href="https://javadoc.io/static/org.kiwiproject/kiwi/4.12.0/org/kiwiproject/base/KiwiEnums.html#streamOf(java.lang.Class)">streamOf</a>.
     * <p>
     * <strong>Either of these methods should be preferred to this method, though {@code listOf}
     * is a direct replacement.</strong>
     * <p>
     * <em>This method may be deprecated in a future release (but probably not).</em>
     *
     * @param enumClass the enum class
     * @param <E>       the type in the enum
     * @return an unmodifiable list containing the enum constants
     * @throws IllegalArgumentException if the given class is not an enum
     */
    public static <E extends Enum<E>> List<E> entries(Class<E> enumClass) {
        checkArgument(enumClass.isEnum(), "%s is not an enum", enumClass);
        return List.of(enumClass.getEnumConstants());
    }
}
