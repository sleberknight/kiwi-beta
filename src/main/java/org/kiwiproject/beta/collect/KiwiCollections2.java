package org.kiwiproject.beta.collect;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Utilities related to Collections.
 * <p>
 * These utilities could be considered for inclusion into kiwi's
 * {@link org.kiwiproject.collect.KiwiCollections} class. Or they
 * could stay here forever in limbo.
 */
@UtilityClass
@Beta
public class KiwiCollections2 {

    /**
     * Finds the first object having type {@code T} in a collection of objects
     * of a base type {@code U}.
     *
     * @param <T> the type to find
     * @param <U> the type of objects in the collection
     * @param theType the type to find
     * @param objects the collection to search; elements may be any subtype of U
     * @return the first object in the collection of U objects having type T
     */
    public static <T extends U, U> Optional<T> findFirstOfType(Class<T> theType,
                                                               Collection<? extends U> objects) {

        checkArgumentNotNull(theType, "type to find must not be null");
        checkArgumentNotNull(objects, "collection must not be null");

        return objects.stream()
            .filter(Objects::nonNull)
            .filter(theType::isInstance)
            .map(theType::cast)
            .findFirst();
    }

    /**
     * Adds the given value to the provided collection only if the condition evaluates to true.
     * <p>
     * This method throws the same exceptions as {@link java.util.Collection#add(Object)}.
     *
     * @param <T>       the element type of the collection
     * @param objects   the collection to which the value may be added
     * @param value     the value to potentially add
     * @param condition the predicate that must be satisfied by {@code value} in order for it to be added
     * @return {@code true} if the value satisfied the condition and was added to the collection; {@code false} otherwise
     */
    @CanIgnoreReturnValue
    public static <T> boolean addIf(Collection<T> objects, @Nullable T value, Predicate<? super T> condition) {
        checkArgumentNotNull(objects, "collection must not be null");
        checkArgumentNotNull(condition, "condition must not be null");

        return condition.test(value) && objects.add(value);
    }
}
