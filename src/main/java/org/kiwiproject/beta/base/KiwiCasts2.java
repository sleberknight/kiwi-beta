package org.kiwiproject.beta.base;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;
import static org.kiwiproject.base.KiwiPreconditions.requirePositive;
import static org.kiwiproject.base.KiwiPreconditions.requirePositiveOrZero;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import lombok.experimental.UtilityClass;
import org.kiwiproject.collect.KiwiCollections;
import org.kiwiproject.collect.KiwiMaps;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utilities related to casting.
 * <p>
 * Some of these methods may be moved into {@code org.kiwiproject.base.KiwiCasts}
 * in <a href="https://github.com/kiwiproject/kiwi">kiwi</a>. As of this writing,
 * {@code KiwiCasts} is not released in kiwi. It is scheduled for the 4.11.0
 * release. It was added to kiwi in
 * <a href="https://github.com/kiwiproject/kiwi/commit/c67493352932985a61db68b839fca1566592efc0">this commit</a>.
 */
@Beta
@UtilityClass
public class KiwiCasts2 {

    /**
     * The default maximum number of non-null checks to perform when checking elements in a collection.
     */
    public static final int DEFAULT_MAX_NON_NULL_CHECKS = 10;

    /**
     * The default maximum number of type checks to perform when checking elements in a collection.
     */
    public static final int DEFAULT_MAX_TYPE_CHECKS = 10;

    private static final CollectionCheckStrategy DEFAULT_COLLECTION_CHECK_STRATEGY =
            new DefaultCollectionCheckStrategy();

    private static final DefaultListCheckStrategy DEFAULT_LIST_CHECK_STRATEGY =
            new DefaultListCheckStrategy();

    private static final SetCheckStrategy DEFAULT_SET_CHECK_STRATEGY =
            new DefaultSetCheckStrategy();

    private static final MapCheckStrategy DEFAULT_MAP_CHECK_STRATEGY =
            new DefaultMapCheckStrategy();


    /**
     * Performs an unchecked cast of the given object to the specified type.
     * <p>
     * <strong>NOTE:</strong> This method is copied directly from {@code KiwiCasts}
     * in kiwi, since it is not currently in a released version of kiwi, and because
     * it is necessary here.
     *
     * @param object the object to cast
     * @param <T>    the type to cast to
     * @return the object cast to the specified type
     */
    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object object) {
        return (T) object;
    }

    /**
     * Strategy interface for checking elements in a collection.
     */
    public interface CollectionCheckStrategy {
        /**
         * Checks that elements in the collection are of the expected type.
         *
         * @param expectedType the expected type of elements in the collection
         * @param coll the collection to check
         * @param <T> the expected element type
         * @return the original collection if all elements match the expected type
         * @throws TypeMismatchException if an element is found with an incompatible type
         */
        <T> Collection<T> checkElements(Class<T> expectedType, Collection<T> coll) throws TypeMismatchException;
    }

    /**
     * Default implementation of {@link CollectionCheckStrategy} that uses
     * {@link #DEFAULT_MAX_NON_NULL_CHECKS} as the maximum non-null checks
     * and checks only one (non-null) element in the collection.
     */
    public static class DefaultCollectionCheckStrategy implements CollectionCheckStrategy {

        private final StandardCollectionCheckStrategy strategy;

        /**
         * Constructs a new instance.
         */
        public DefaultCollectionCheckStrategy() {
            strategy = StandardCollectionCheckStrategy.of(DEFAULT_MAX_NON_NULL_CHECKS, 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Collection<T> checkElements(Class<T> expectedType, Collection<T> coll) {
            return strategy.checkElements(expectedType, coll);
        }
    }

    private record ElementCheckResult(boolean ok, Object invalidValue) {
        static ElementCheckResult okCollection() {
            return new ElementCheckResult(true, null);
        }

        static ElementCheckResult foundInvalidType(Object value) {
            checkArgumentNotNull(value, "value must not be null");
            return new ElementCheckResult(false, value);
        }
    }

    /**
     * Standard implementation of {@link CollectionCheckStrategy} that allows configuring
     * the number of non-null and type checks to perform.
     */
    public static class StandardCollectionCheckStrategy implements CollectionCheckStrategy {

        private final int maxNonNullChecks;
        private final int maxElementTypeChecks;

        private StandardCollectionCheckStrategy(int maxNonNullChecks, int maxElementTypeChecks) {
            this.maxNonNullChecks = requirePositiveOrZero(maxNonNullChecks);
            this.maxElementTypeChecks = requirePositive(maxElementTypeChecks);
        }

        /**
         * Creates a new instance with default settings for maximum non-null and type checks.
         * <p>
         * Uses {@link #DEFAULT_MAX_NON_NULL_CHECKS} and {@link #DEFAULT_MAX_TYPE_CHECKS} as the
         * values for {@code maxNonNullChecks} and {@code maxElementTypeChecks}, respectively.
         *
         * @return a new instance
         */
        public static StandardCollectionCheckStrategy ofDefaults() {
            return new StandardCollectionCheckStrategy(DEFAULT_MAX_NON_NULL_CHECKS, DEFAULT_MAX_TYPE_CHECKS);
        }

        /**
         * Creates a new instance with the specified maximum non-null and type checks.
         *
         * @param maxNonNullChecks the maximum number of non-null checks to perform
         * @param maxElementTypeChecks the maximum number of element type checks to perform
         * @return a new instance with the specified settings
         */
        public static StandardCollectionCheckStrategy of(int maxNonNullChecks, int maxElementTypeChecks) {
            return new StandardCollectionCheckStrategy(maxNonNullChecks, maxElementTypeChecks);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Collection<T> checkElements(Class<T> expectedType, Collection<T> coll) throws TypeMismatchException {
            var checkResult = checkElementsStandardStrategy(expectedType, coll, maxNonNullChecks, maxElementTypeChecks);

            if (checkResult.ok()) {
                return coll;
            }

            throw newCollectionTypeMismatch(Collection.class, expectedType, checkResult);
        }
    }

    /**
     * Casts the given object to a Collection and checks that its elements are of the expected type.
     * Uses {@link DefaultCollectionCheckStrategy} as the collection check strategy.
     *
     * @param expectedType the expected type of elements in the collection
     * @param object the object to cast to a Collection
     * @param <T> the expected element type
     * @return the object cast to a Collection with elements of the expected type
     * @throws TypeMismatchException if the object is not a Collection or contains elements of incompatible types
     */
    public static <T> Collection<T> castToCollectionAndCheckElements(Class<T> expectedType, Object object) {
        return castToCollectionAndCheckElements(expectedType, object, DEFAULT_COLLECTION_CHECK_STRATEGY);
    }

    /**
     * Casts the given object to a Collection and checks that its elements are of the expected type
     * using the specified strategy.
     *
     * @param expectedType the expected type of elements in the collection
     * @param object the object to cast to a Collection
     * @param strategy the strategy to use for checking elements
     * @param <T> the expected element type
     * @return the object cast to a Collection with elements of the expected type
     * @throws TypeMismatchException if the object is not a Collection or contains elements of incompatible types
     */
    public static <T> Collection<T> castToCollectionAndCheckElements(Class<T> expectedType,
                                                                     Object object,
                                                                     CollectionCheckStrategy strategy) {
        checkExpectedTypeNotNull(expectedType);
        try {
            Collection<T> coll = uncheckedCast(object);
            return strategy.checkElements(expectedType, coll);
        } catch (ClassCastException e) {
            throw TypeMismatchException.forTypeMismatch(Collection.class, e);
        }
    }

    /**
     * Strategy interface for checking elements in a list.
     */
    public interface ListCheckStrategy {
        /**
         * Checks that elements in the list are of the expected type.
         *
         * @param expectedType the expected type of elements in the list
         * @param list the list to check
         * @param <T> the expected element type
         * @return the original list if all elements match the expected type
         * @throws TypeMismatchException if an element is found with an incompatible type
         */
        <T> List<T> checkElements(Class<T> expectedType, List<T> list) throws TypeMismatchException;
    }

    /**
     * Default implementation of {@link ListCheckStrategy} that uses
     * {@link #DEFAULT_MAX_NON_NULL_CHECKS} as the maximum non-null checks
     * and checks only one (non-null) element in the list.
     */
    public static class DefaultListCheckStrategy implements ListCheckStrategy {

        private final StandardListCheckStrategy strategy;

        /**
         * Constructs a new instance.
         */
        public DefaultListCheckStrategy() {
            strategy = StandardListCheckStrategy.of(DEFAULT_MAX_NON_NULL_CHECKS, 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> List<T> checkElements(Class<T> expectedType, List<T> list) {
            return strategy.checkElements(expectedType, list);
        }
    }

    /**
     * Standard implementation of {@link ListCheckStrategy} that allows configuring
     * the number of non-null and type checks to perform.
     */
    public static class StandardListCheckStrategy implements ListCheckStrategy {

        private final int maxNonNullChecks;
        private final int maxElementTypeChecks;

        private StandardListCheckStrategy(int maxNonNullChecks, int maxElementTypeChecks) {
            this.maxNonNullChecks = requirePositiveOrZero(maxNonNullChecks);
            this.maxElementTypeChecks = requirePositive(maxElementTypeChecks);
        }

        /**
         * Creates a new instance with default settings for maximum non-null and type checks.
         * <p>
         * Uses {@link #DEFAULT_MAX_NON_NULL_CHECKS} and {@link #DEFAULT_MAX_TYPE_CHECKS} as the
         * values for {@code maxNonNullChecks} and {@code maxElementTypeChecks}, respectively.
         *
         * @return a new instance
         */
        public static StandardListCheckStrategy ofDefaults() {
            return new StandardListCheckStrategy(DEFAULT_MAX_NON_NULL_CHECKS, DEFAULT_MAX_TYPE_CHECKS);
        }

        /**
         * Creates a new instance with the specified maximum non-null and type checks.
         *
         * @param maxNonNullChecks the maximum number of non-null checks to perform
         * @param maxElementTypeChecks the maximum number of element type checks to perform
         * @return a new instance with the specified settings
         */
        public static StandardListCheckStrategy of(int maxNonNullChecks, int maxElementTypeChecks) {
            return new StandardListCheckStrategy(maxNonNullChecks, maxElementTypeChecks);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> List<T> checkElements(Class<T> expectedType, List<T> list) throws TypeMismatchException {
            var checkResult = checkElementsStandardStrategy(expectedType, list, maxNonNullChecks, maxElementTypeChecks);

            if (checkResult.ok()) {
                return list;
            }

            throw newCollectionTypeMismatch(List.class, expectedType, checkResult);
        }
    }

    /**
     * Casts the given object to a List and checks that its elements are of the expected type.
     * Uses {@link DefaultListCheckStrategy} as the list check strategy.
     *
     * @param expectedType the expected type of elements in the list
     * @param object the object to cast to a List
     * @param <T> the expected element type
     * @return the object cast to a List with elements of the expected type
     * @throws TypeMismatchException if the object is not a List or contains elements of incompatible types
     */
    public static <T> List<T> castToListAndCheckElements(Class<T> expectedType, Object object) {
        return castToListAndCheckElements(expectedType, object, DEFAULT_LIST_CHECK_STRATEGY);
    }

    /**
     * Casts the given object to a List and checks that its elements are of the expected type
     * using the specified check strategy.
     *
     * @param expectedType the expected type of elements in the list
     * @param object the object to cast to a List
     * @param strategy the strategy to use for checking elements
     * @param <T> the expected element type
     * @return the object cast to a List with elements of the expected type
     * @throws TypeMismatchException if the object is not a List or contains elements of incompatible types
     */
    public static <T> List<T> castToListAndCheckElements(Class<T> expectedType,
                                                         Object object,
                                                         ListCheckStrategy strategy) {
        checkExpectedTypeNotNull(expectedType);
        try {
            List<T> list = uncheckedCast(object);
            return strategy.checkElements(expectedType, list);
        } catch (ClassCastException e) {
            throw TypeMismatchException.forTypeMismatch(List.class, e);
        }
    }

    /**
     * Strategy interface for checking elements in a set.
     */
    public interface SetCheckStrategy {
        /**
         * Checks that elements in the set are of the expected type.
         *
         * @param expectedType the expected type of elements in the set
         * @param set the set to check
         * @param <T> the expected element type
         * @return the original set if all elements match the expected type
         * @throws TypeMismatchException if an element is found with an incompatible type
         */
        <T> Set<T> checkElements(Class<T> expectedType, Set<T> set) throws TypeMismatchException;
    }

    /**
     * Default implementation of {@link SetCheckStrategy} that uses
     * {@link #DEFAULT_MAX_NON_NULL_CHECKS} as the maximum non-null checks
     * and checks only one (non-null) element in the set.
     */
    public static class DefaultSetCheckStrategy implements SetCheckStrategy {

        private final StandardSetCheckStrategy strategy;

        /**
         * Constructs a new instance.
         */
        public DefaultSetCheckStrategy() {
            strategy = StandardSetCheckStrategy.of(DEFAULT_MAX_NON_NULL_CHECKS, 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Set<T> checkElements(Class<T> expectedType, Set<T> set) {
            return strategy.checkElements(expectedType, set);
        }
    }

    /**
     * Standard implementation of {@link SetCheckStrategy} that allows configuring
     * the number of non-null and type checks to perform.
     */
    public static class StandardSetCheckStrategy implements SetCheckStrategy {

        private final int maxNonNullChecks;
        private final int maxElementTypeChecks;

        private StandardSetCheckStrategy(int maxNonNullChecks, int maxElementTypeChecks) {
            this.maxNonNullChecks = requirePositiveOrZero(maxNonNullChecks);
            this.maxElementTypeChecks = requirePositive(maxElementTypeChecks);
        }

        /**
         * Creates a new instance with default settings for maximum non-null and type checks.
         * <p>
         * Uses {@link #DEFAULT_MAX_NON_NULL_CHECKS} and {@link #DEFAULT_MAX_TYPE_CHECKS} as the
         * values for {@code maxNonNullChecks} and {@code maxElementTypeChecks}, respectively.
         *
         * @return a new instance
         */
        public static StandardSetCheckStrategy ofDefaults() {
            return new StandardSetCheckStrategy(DEFAULT_MAX_NON_NULL_CHECKS, DEFAULT_MAX_TYPE_CHECKS);
        }

        /**
         * Creates a new instance with the specified maximum non-null and type checks.
         *
         * @param maxNonNullChecks the maximum number of non-null checks to perform
         * @param maxElementTypeChecks the maximum number of element type checks to perform
         * @return a new instance with the specified settings
         */
        public static StandardSetCheckStrategy of(int maxNonNullChecks, int maxElementTypeChecks) {
            return new StandardSetCheckStrategy(maxNonNullChecks, maxElementTypeChecks);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Set<T> checkElements(Class<T> expectedType, Set<T> set) throws TypeMismatchException {
            var checkResult = checkElementsStandardStrategy(expectedType, set, maxNonNullChecks, maxElementTypeChecks);

            if (checkResult.ok()) {
                return set;
            }

            throw newCollectionTypeMismatch(Set.class, expectedType, checkResult);
        }
    }

    private static <T> ElementCheckResult checkElementsStandardStrategy(Class<?> expectedType,
                                                                        Collection<T> coll,
                                                                        int maxNonNullChecks,
                                                                        int maxElementTypeChecks) {

        if (KiwiCollections.isNullOrEmpty(coll)) {
            // We can't verify type information about a null or empty collection
            return ElementCheckResult.okCollection();
        }

        var iterator = coll.iterator();
        var nullCheckCount = 0;
        var typeCheckCount = 0;

        while (iterator.hasNext()) {
            T value = iterator.next();

            if (isNull(value)) {
                nullCheckCount++;
                if (nullCheckCount > maxNonNullChecks) {
                    return ElementCheckResult.okCollection();
                }
            } else if (isNotExpectedType(expectedType, value)) {
                return ElementCheckResult.foundInvalidType(value);
            } else {
                typeCheckCount++;
                if (typeCheckCount >= maxElementTypeChecks) {
                    break;
                }
            }
        }

        return ElementCheckResult.okCollection();
    }

    private static TypeMismatchException newCollectionTypeMismatch(Class<?> collectionType, Class<?> expectedType, ElementCheckResult checkResult) {
        return TypeMismatchException.forCollectionTypeMismatch(collectionType, expectedType, checkResult.invalidValue().getClass());
    }

    /**
     * Casts the given object to a Set and checks that its elements are of the expected type.
     * Uses {@link DefaultSetCheckStrategy} as the set check strategy.
     *
     * @param expectedType the expected type of elements in the set
     * @param object the object to cast to a Set
     * @param <T> the expected element type
     * @return the object cast to a Set with elements of the expected type
     * @throws TypeMismatchException if the object is not a Set or contains elements of incompatible types
     */
    public static <T> Set<T> castToSetAndCheckElements(Class<T> expectedType, Object object) {
        return castToSetAndCheckElements(expectedType, object, DEFAULT_SET_CHECK_STRATEGY);
    }

    /**
     * Casts the given object to a Set and checks that its elements are of the expected type
     * using the specified check strategy.
     *
     * @param expectedType the expected type of elements in the set
     * @param object the object to cast to a Set
     * @param strategy the strategy to use for checking elements
     * @param <T> the expected element type
     * @return the object cast to a Set with elements of the expected type
     * @throws TypeMismatchException if the object is not a Set or contains elements of incompatible types
     */
    public static <T> Set<T> castToSetAndCheckElements(Class<T> expectedType,
                                                       Object object,
                                                       SetCheckStrategy strategy) {
        checkExpectedTypeNotNull(expectedType);
        try {
            Set<T> set = uncheckedCast(object);
            return strategy.checkElements(expectedType, set);
        } catch (ClassCastException e) {
            throw TypeMismatchException.forTypeMismatch(Set.class, e);
        }
    }

    private static <T> void checkExpectedTypeNotNull(Class<T> expectedType) {
        checkArgumentNotNull(expectedType, "expectedType must not be null");
    }

    /**
     * Strategy interface for checking entries in a map.
     */
    public interface MapCheckStrategy {
        /**
         * Checks that keys and values in the map are of the expected types.
         *
         * @param keyType the expected type of keys in the map
         * @param valueType the expected type of values in the map
         * @param map the map to check
         * @param <K> the expected key type
         * @param <V> the expected value type
         * @return the original map if all keys and values match the expected types
         * @throws TypeMismatchException if a key or value is found with an incompatible type
         */
        <K, V> Map<K, V> checkEntries(Class<K> keyType, Class<V> valueType, Map<K, V> map) throws TypeMismatchException;
    }

    /**
     * Default implementation of {@link MapCheckStrategy} that uses
     * {@link #DEFAULT_MAX_NON_NULL_CHECKS} as the maximum non-null checks
     * and checks only one (non-null) entry in the map.
     */
    public static class DefaultMapCheckStrategy implements MapCheckStrategy {

        private final StandardMapCheckStrategy strategy;

        /**
         * Constructs a new instance with default settings.
         */
        public DefaultMapCheckStrategy() {
            strategy = StandardMapCheckStrategy.of(DEFAULT_MAX_NON_NULL_CHECKS, 1);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> Map<K, V> checkEntries(Class<K> keyType, Class<V> valueType, Map<K, V> map) {
            return strategy.checkEntries(keyType, valueType, map);
        }
    }

    /**
     * Standard implementation of {@link MapCheckStrategy} that allows configuring
     * the number of non-null and type checks to perform.
     */
    public static class StandardMapCheckStrategy implements MapCheckStrategy {

        private final int maxNonNullChecks;
        private final int maxEntryTypeChecks;

        private StandardMapCheckStrategy(int maxNonNullChecks, int maxEntryTypeChecks) {
            this.maxNonNullChecks = requirePositiveOrZero(maxNonNullChecks);
            this.maxEntryTypeChecks = requirePositive(maxEntryTypeChecks);
        }

        /**
         * Creates a new instance with default settings for maximum non-null and type checks.
         * <p>
         * Uses {@link #DEFAULT_MAX_NON_NULL_CHECKS} and {@link #DEFAULT_MAX_TYPE_CHECKS} as the
         * values for {@code maxNonNullChecks} and {@code maxElementTypeChecks}, respectively.
         *
         * @return a new instance
         */
        public static StandardMapCheckStrategy ofDefaults() {
            return new StandardMapCheckStrategy(DEFAULT_MAX_NON_NULL_CHECKS, DEFAULT_MAX_TYPE_CHECKS);
        }

        /**
         * Creates a new instance with the specified maximum non-null and type checks.
         *
         * @param maxNonNullChecks the maximum number of non-null checks to perform
         * @param maxElementTypeChecks the maximum number of entry type checks to perform
         * @return a new instance with the specified settings
         */
        public static StandardMapCheckStrategy of(int maxNonNullChecks, int maxElementTypeChecks) {
            return new StandardMapCheckStrategy(maxNonNullChecks, maxElementTypeChecks);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <K, V> Map<K, V> checkEntries(Class<K> keyType, Class<V> valueType, Map<K, V> map) {
            var checkResult = checkEntriesInternal(keyType, valueType, map);

            if (checkResult.ok()) {
                return map;
            }

            var entryType = requireNonNull(checkResult.entryType(),
                    "entryType must not be null when result is not ok");

            if (entryType == EntryType.KEY) {
                throw TypeMismatchException.forMapKeyTypeMismatch(keyType, checkResult.invalidValue().getClass());
            }

            checkEntryTypeIsValue(checkResult);
            throw TypeMismatchException.forMapValueTypeMismatch(valueType, checkResult.invalidValue().getClass());
        }

        @VisibleForTesting
        static void checkEntryTypeIsValue(EntryCheckResult checkResult) {
            checkState(checkResult.entryType() == EntryType.VALUE,
                    "EntryCheckResult has unexpected entryType: %s", checkResult.entryType());
        }

        enum EntryType {
            KEY, VALUE
        }

        @VisibleForTesting
        record EntryCheckResult(boolean ok, EntryType entryType, Object invalidValue) {
            static EntryCheckResult okMap() {
                return new EntryCheckResult(true, null, null);
            }

            static EntryCheckResult foundInvalidType(EntryType entryType, Object value) {
                checkArgumentNotNull(entryType, "entryType must not be null");
                checkArgumentNotNull(value, "value must not be null");
                return new EntryCheckResult(false, entryType, value);
            }
        }

        private <K, V> EntryCheckResult checkEntriesInternal(
                Class<?> expectedKeyType,
                Class<?> expectedValueType,
                Map<K, V> map) {

            if (KiwiMaps.isNullOrEmpty(map)) {
                // We can't verify type information about a null or empty map
                return EntryCheckResult.okMap();
            }

            var iterator = map.entrySet().iterator();
            var nullCheckCount = 0;
            var typeCheckCount = 0;

            while (iterator.hasNext()) {
                Map.Entry<K, V> entry = iterator.next();
                K key = entry.getKey();
                V value = entry.getValue();

                var keyIsNotNull = nonNull(key);
                var valueIsNotNull = nonNull(value);

                if ((isNull(key) || isNull(value)) && ++nullCheckCount > maxNonNullChecks) {
                    return EntryCheckResult.okMap();
                }

                if (keyIsNotNull && isNotExpectedType(expectedKeyType, key)) {
                    return EntryCheckResult.foundInvalidType(EntryType.KEY, key);
                }

                if (valueIsNotNull && isNotExpectedType(expectedValueType, value)) {
                    return EntryCheckResult.foundInvalidType(EntryType.VALUE, value);
                }

                if ((keyIsNotNull || valueIsNotNull) && ++typeCheckCount >= maxEntryTypeChecks) {
                    break;
                }
            }

            return EntryCheckResult.okMap();
        }
    }

    /**
     * Casts the given object to a Map and checks that its keys and values are of the expected types.
     * Uses {@link DefaultMapCheckStrategy} as the map check strategy.
     *
     * @param keyType the expected type of keys in the map
     * @param valueType the expected type of values in the map
     * @param object the object to cast to a Map
     * @param <K> the expected key type
     * @param <V> the expected value type
     * @return the object cast to a Map with keys and values of the expected types
     * @throws TypeMismatchException if the object is not a Map or contains keys or values of incompatible types
     */
    public static <K, V> Map<K, V> castToMapAndCheckEntries(Class<K> keyType, Class<V> valueType, Object object) {
        return castToMapAndCheckEntries(keyType, valueType, object, DEFAULT_MAP_CHECK_STRATEGY);
    }

    /**
     * Casts the given object to a Map and checks that its keys and values are of the expected types
     * using the specified check strategy.
     *
     * @param keyType the expected type of keys in the map
     * @param valueType the expected type of values in the map
     * @param object the object to cast to a Map
     * @param strategy the strategy to use for checking entries
     * @param <K> the expected key type
     * @param <V> the expected value type
     * @return the object cast to a Map with keys and values of the expected types
     * @throws TypeMismatchException if the object is not a Map or contains keys or values of incompatible types
     */
    public static <K, V> Map<K, V> castToMapAndCheckEntries(Class<K> keyType,
                                                            Class<V> valueType,
                                                            Object object,
                                                            MapCheckStrategy strategy) {

        checkArgumentNotNull(keyType, "keyType must not be null");
        checkArgumentNotNull(valueType, "valueType must not be null");
        try {
            Map<K, V> map = uncheckedCast(object);
            return strategy.checkEntries(keyType, valueType, map);
        } catch (ClassCastException e) {
            throw TypeMismatchException.forTypeMismatch(Map.class, e);
        }
    }

    private static boolean isNotExpectedType(Class<?> expectedType, Object object) {
        return !isExpectedType(expectedType, object);
    }

    private static boolean isExpectedType(Class<?> expectedType, Object object) {
        return expectedType.isAssignableFrom(object.getClass());
    }
}
