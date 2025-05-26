package org.kiwiproject.beta.base;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.Streams;
import lombok.experimental.UtilityClass;
import org.kiwiproject.collect.KiwiCollections;
import org.kiwiproject.collect.KiwiMaps;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    private static final int DEFAULT_MAX_NON_NULL_CHECKS = 10;
    private static final int DEFAULT_MAX_TYPE_CHECKS = 10;

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

    public interface CollectionCheckStrategy {
        <T> Collection<T> checkElements(Class<T> expectedType, Collection<T> coll) throws TypeMismatchException;
    }

    public static class DefaultCollectionCheckStrategy implements CollectionCheckStrategy {

        private final StandardCollectionCheckStrategy strategy;

        public DefaultCollectionCheckStrategy() {
            strategy = StandardCollectionCheckStrategy.of(DEFAULT_MAX_NON_NULL_CHECKS, 1);
        }

        @Override
        public <T> Collection<T> checkElements(Class<T> expectedType, Collection<T> coll) {
            return strategy.checkElements(expectedType, coll);
        }
    }

    public static class StandardCollectionCheckStrategy implements CollectionCheckStrategy {

        private final int maxNonNullChecks;
        private final int maxElementTypeChecks;

        private StandardCollectionCheckStrategy(int maxNonNullChecks, int maxElementTypeChecks) {
            this.maxNonNullChecks = maxNonNullChecks;
            this.maxElementTypeChecks = maxElementTypeChecks;
        }

        public static StandardCollectionCheckStrategy ofDefaults() {
            return new StandardCollectionCheckStrategy(DEFAULT_MAX_NON_NULL_CHECKS, DEFAULT_MAX_TYPE_CHECKS);
        }

        public static StandardCollectionCheckStrategy of(int maxNonNullChecks, int maxElementTypeChecks) {
            return new StandardCollectionCheckStrategy(maxNonNullChecks, maxElementTypeChecks);
        }

        @Override
        public <T> Collection<T> checkElements(Class<T> expectedType, Collection<T> coll) throws TypeMismatchException {
            var checkResult = checkElementsStandardStrategy(expectedType, coll, maxNonNullChecks, maxElementTypeChecks);

            if (checkResult.ok()) {
                return coll;
            }

            throw newCollectionTypeMismatch(Collection.class, expectedType, checkResult);
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

    public static <T> Collection<T> castToCollectionAndCheckElements(Class<T> expectedType, Object object) {
        return castToCollectionAndCheckElements(expectedType, object, DEFAULT_COLLECTION_CHECK_STRATEGY);
    }

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

    public interface ListCheckStrategy {
        <T> List<T> checkElements(Class<T> expectedType, List<T> list) throws TypeMismatchException;
    }

    public static class DefaultListCheckStrategy implements ListCheckStrategy {

        private final StandardListCheckStrategy strategy;

        public DefaultListCheckStrategy() {
            strategy = StandardListCheckStrategy.of(DEFAULT_MAX_NON_NULL_CHECKS, 1);
        }

        @Override
        public <T> List<T> checkElements(Class<T> expectedType, List<T> list) {
            return strategy.checkElements(expectedType, list);
        }
    }

    public static class StandardListCheckStrategy implements ListCheckStrategy {

        private final int maxNonNullChecks;
        private final int maxElementTypeChecks;

        private StandardListCheckStrategy(int maxNonNullChecks, int maxElementTypeChecks) {
            this.maxNonNullChecks = maxNonNullChecks;
            this.maxElementTypeChecks = maxElementTypeChecks;
        }

        public static StandardListCheckStrategy ofDefaults() {
            return new StandardListCheckStrategy(DEFAULT_MAX_NON_NULL_CHECKS, DEFAULT_MAX_TYPE_CHECKS);
        }

        public static StandardListCheckStrategy of(int maxNonNullChecks, int maxElementTypeChecks) {
            return new StandardListCheckStrategy(maxNonNullChecks, maxElementTypeChecks);
        }

        @Override
        public <T> List<T> checkElements(Class<T> expectedType, List<T> coll) throws TypeMismatchException {
            var checkResult = checkElementsStandardStrategy(expectedType, coll, maxNonNullChecks, maxElementTypeChecks);

            if (checkResult.ok()) {
                return coll;
            }

            throw newCollectionTypeMismatch(List.class, expectedType, checkResult);
        }
    }

    public static <T> List<T> castToListAndCheckElements(Class<T> expectedType, Object object) {
        return castToListAndCheckElements(expectedType, object, DEFAULT_LIST_CHECK_STRATEGY);
    }

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

    public interface SetCheckStrategy {
        <T> Set<T> checkElements(Class<T> expectedType, Set<T> set) throws TypeMismatchException;
    }

    public static class DefaultSetCheckStrategy implements SetCheckStrategy {

        @Override
        public <T> Set<T> checkElements(Class<T> expectedType, Set<T> set) {
            var checkResult = checkElementsDefaultStrategy(expectedType, set);

            if (checkResult.ok()) {
                return set;
            }

            throw newCollectionTypeMismatch(Set.class, expectedType, checkResult);
        }
    }

    record ElementCheckResult(boolean ok, Object invalidValue) {
        static ElementCheckResult okCollection() {
            return new ElementCheckResult(true, null);
        }

        static ElementCheckResult foundInvalidType(Object value) {
            checkArgumentNotNull(value, "value must not be null");
            return new ElementCheckResult(false, value);
        }
    }

    private static <T> ElementCheckResult checkElementsDefaultStrategy(Class<?> expectedType, Collection<T> coll) {
        if (KiwiCollections.isNullOrEmpty(coll)) {
            // We can't verify type information about a null or empty collection
            return ElementCheckResult.okCollection();
        }

        var maybeFirstNonNullValue = findFirstNonNullValueOrNull(coll);
        if (isNull(maybeFirstNonNullValue)) {
            // We didn't find a non-null value quickly, so give up
            return ElementCheckResult.okCollection();
        }

        if (isNotExpectedType(expectedType, maybeFirstNonNullValue)) {
            // We found a non-null value, but it is not assignable to the expected type, so the collection is invalid
            return ElementCheckResult.foundInvalidType(maybeFirstNonNullValue);
        }

        return ElementCheckResult.okCollection();
    }

    private static TypeMismatchException newCollectionTypeMismatch(Class<?> collectionType, Class<?> expectedType, ElementCheckResult checkResult) {
        return TypeMismatchException.forCollectionTypeMismatch(collectionType, expectedType, checkResult.invalidValue().getClass());
    }

    public static <T> Set<T> castToSetAndCheckElements(Class<T> expectedType, Object object) {
        return castToSetAndCheckElements(expectedType, object, DEFAULT_SET_CHECK_STRATEGY);
    }

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

    private static <T> T findFirstNonNullValueOrNull(Collection<T> coll) {
        return coll.stream()
                .filter(Objects::nonNull)
                .limit(DEFAULT_MAX_NON_NULL_CHECKS)
                .findFirst()
                .orElse(null);
    }

    private static <T> T findFirstNonNullValueOrNull(Iterator<T> iterator) {
        return Streams.stream(iterator)
                .filter(Objects::nonNull)
                .limit(DEFAULT_MAX_NON_NULL_CHECKS)
                .findFirst()
                .orElse(null);
    }

    public interface MapCheckStrategy {
        <K, V> Map<K, V> checkEntries(Class<K> keyType, Class<V> valueType, Map<K, V> map) throws TypeMismatchException;
    }

    public static class DefaultMapCheckStrategy implements MapCheckStrategy {

        @Override
        public <K, V> Map<K, V> checkEntries(Class<K> keyType, Class<V> valueType, Map<K, V> map) {
            if (KiwiMaps.isNullOrEmpty(map)) {
                // We can't verify type information about a null or empty map
                return map;
            }

            // Find first non-null key with non-null value
            Map.Entry<K, V> firstNonNullEntry = map.entrySet().stream()
                    .filter(entry -> nonNull(entry.getKey()) && nonNull(entry.getValue()))
                    .limit(DEFAULT_MAX_NON_NULL_CHECKS)
                    .findFirst()
                    .orElse(null);

            if (isNull(firstNonNullEntry)) {
                // We didn't find an entry with a non-null key and value quickly, so give up
                return map;
            }

            var key = firstNonNullEntry.getKey();

            if (isExpectedType(keyType, key)) {
                var theValue = map.get(key);
                if (isExpectedType(valueType, theValue)) {
                    return map;
                } else {
                    throw TypeMismatchException.forMapValueTypeMismatch(valueType, theValue.getClass());
                }
            }

            throw TypeMismatchException.forMapKeyTypeMismatch(keyType, key.getClass());
        }
    }

    public static <K, V> Map<K, V> castToMapAndCheckEntries(Class<K> keyType, Class<V> valueType, Object object) {
        return castToMapAndCheckEntries(keyType, valueType, object, DEFAULT_MAP_CHECK_STRATEGY);
    }

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
