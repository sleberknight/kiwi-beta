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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

        checkTypeAndObjects(theType, objects);

        return findFirstOfType(theType, (Iterable<? extends U>) objects);
    }

    /**
     * Finds the first object having type {@code T} in an {@link Iterable} of objects of base type {@code U}.
     *
     * @param <T> the type to find
     * @param <U> the base type of the objects
     * @param theType the type to find
     * @param objects the iterable to search; elements may be any subtype of U
     * @return the first object in the iterable of U objects having type T
     */
    public static <T extends U, U> Optional<T> findFirstOfType(Class<T> theType,
                                                               Iterable<? extends U> objects) {
        checkTypeAndObjects(theType, objects);

        return findFirstOfType(theType, StreamSupport.stream(objects.spliterator(), false));
    }

    /**
     * Finds the first object having type {@code T} in a {@link Stream} of objects of base type {@code U}.
     *
     * <p>Note: This method will consume the provided stream.</p>
     *
     * @param <T> the type to find
     * @param <U> the base type of the objects
     * @param theType the type to find
     * @param objects the stream to search; elements may be any subtype of U
     * @return the first object in the stream of U objects having type T
     */
    public static <T extends U, U> Optional<T> findFirstOfType(Class<T> theType,
                                                               Stream<? extends U> objects) {

        checkTypeAndObjects(theType, objects);

        return objects
            .filter(Objects::nonNull)
            .filter(theType::isInstance)
            .map(theType::cast)
            .findFirst();
    }

    private static <T extends U, U> void checkTypeAndObjects(Class<T> theType, Object objects) {
        checkArgumentNotNull(theType, "type to find must not be null");
        checkArgumentNotNull(objects, "objects to find must not be null");
    }

    /**
     * Adds the given value to the provided collection only if the value is non-null.
     * <p>
     * If {@code value} is {@code null}, this method does nothing and returns {@code false}.
     * <p>
     * This method throws the same exceptions as {@link java.util.Collection#add(Object)} when the
     * value is non-null and the underlying collection rejects the addition (e.g., is unmodifiable).
     *
     * @param <T>     the element type of the collection
     * @param objects the collection to which the value may be added
     * @param value   the value to potentially add; if {@code null}, it will not be added
     * @return {@code true} if the value was non-null and was added to the collection; {@code false} otherwise
     * @throws IllegalArgumentException if {@code objects} is {@code null}
     * @see #addIf(Collection, Object, Predicate)
     */
    public static <T> boolean addIfNonNull(Collection<T> objects, @Nullable T value) {
        return addIf(objects, value, Objects::nonNull);
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
