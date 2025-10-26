package org.kiwiproject.beta.collect;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * Extension methods for working with {@link Collection} instances.
 * <p>
 * These methods are designed to be used with Lombok's {@code @ExtensionMethod} to enable a fluent,
 * Kotlin-like style on Java collections.
 */
@Beta
@UtilityClass
public class KiwiCollectionExtensions {

    /**
     * Adds the given value to the provided collection only if the condition evaluates to true.
     * <p>
     * This is an extension-friendly wrapper around {@link KiwiCollections2#addIf(Collection, Object, Predicate)}
     * allowing calls like {@code collection.addIf(value, condition)} when using Lombok's
     * {@code @ExtensionMethod(KiwiCollectionExtensions.class)}.
     *
     * @param <T>       the element type of the collection
     * @param objects   the collection to which the value may be added
     * @param value     the value to potentially add
     * @param condition the predicate that must be satisfied by {@code value} in order for it to be added
     * @return {@code true} if the value satisfied the condition and was added to the collection; {@code false} otherwise
     * @see KiwiCollections2#addIf(Collection, Object, Predicate)
     */
    @CanIgnoreReturnValue
    public static <T> boolean addIf(Collection<T> objects, @Nullable T value, Predicate<? super T> condition) {
        return KiwiCollections2.addIf(objects, value, condition);
    }
}
