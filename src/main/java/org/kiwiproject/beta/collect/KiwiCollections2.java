package org.kiwiproject.beta.collect;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
}
