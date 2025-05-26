package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.collect.KiwiLists;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@DisplayName("KiwiCasts2")
class KiwiCasts2Test {

    @Nested
    class CastToCollectionAndCheckElements {

        @Nested
        class UsingDefaultCollectionCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnCollection_WhenIsNullOrEmpty(Collection<?> coll) {
                Collection<String> stringColl = KiwiCasts2.castToCollectionAndCheckElements(String.class, coll);
                assertThat(stringColl).isSameAs(coll);
            }

            @Test
            void shouldReturnCollection_WhenAllElementsAreNull() {
                Object o = Lists.newArrayList(null, null, null, null, null, null);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_WhenExceedMaxNulls() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_WhenContainsExpectedType() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_ThatThrowsClassCast_WhenExceedMaxNulls_ButDidNotDetectBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g", 42);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);

                assertThat(coll).isSameAs(o);
                var list = coll.stream().toList();
                assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
                    // We must do the assignment to get the ClassCastException
                    @SuppressWarnings("unused") String last = KiwiLists.last(list);
                });
            }

            @Test
            void shouldThrowTypeMismatchException_WhenFindBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, 1, null, 2, 3, 4, 5, 6);
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToCollectionAndCheckElements(String.class, o))
                        .withMessage("Expected java.util.Collection to contain elements of type java.lang.String, but found element of type java.lang.Integer");
            }
        }

        @Nested
        class UsingStandardCollectionCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnCollection_WhenIsNullOrEmpty(Collection<?> coll) {
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults();
                Collection<String> stringColl = KiwiCasts2.castToCollectionAndCheckElements(String.class, coll, strategy);
                assertThat(stringColl).isSameAs(coll);
            }

            @Test
            void shouldReturnCollection_WhenAllElementsAreNull() {
                Object o = Lists.newArrayList(null, null, null, null, null, null);
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults();
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, strategy);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_WhenExceedMaxNulls() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults();
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, strategy);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_WhenContainsExpectedType() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e");
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults();
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, strategy);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_WhenFewerElementsThanMaxTypeChecks() {
                Object o = Lists.newArrayList("a", "b", "c");
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.of(5, 20);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, strategy);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_WhenMoreElementsThanMaxTypeChecks() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.of(5, 15);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, strategy);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_ThatThrowsClassCast_WhenExceedMaxNulls_ButDidNotDetectBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", 42);
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.of(3, 5);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, strategy);

                assertThat(coll).isSameAs(o);
                var list = coll.stream().toList();
                assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
                    // We must do the assignment to get the ClassCastException
                    @SuppressWarnings("unused") String last = KiwiLists.last(list);
                });
            }

            @Test
            void shouldThrowTypeMismatchException_WhenIsNotCollection() {
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToCollectionAndCheckElements(String.class, "not a collection"))
                        .withMessage("Cannot cast value to type java.util.Collection")
                        .withCauseInstanceOf(ClassCastException.class);
            }

            @Test
            void shouldThrowTypeMismatchException_WhenFindBadType() {
                Object o = Lists.newArrayList(null, null, 1, null, 2, 3, 4, 5, 6);
                var strategy = KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults();
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToCollectionAndCheckElements(String.class, o, strategy))
                        .withMessage("Expected java.util.Collection to contain elements of type java.lang.String, but found element of type java.lang.Integer");
            }
        }
    }

    @Nested
    class CastToListAndCheckElements {

        @Nested
        class UsingDefaultListCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnList_WhenIsNullOrEmpty(List<?> coll) {
                List<String> stringList = KiwiCasts2.castToListAndCheckElements(String.class, coll);
                assertThat(stringList).isSameAs(coll);
            }

            @Test
            void shouldReturnList_WhenAllElementsAreNull() {
                Object o = Lists.newArrayList(null, null, null, null, null, null);
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnList_WhenExceedMaxNulls() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnList_WhenContainsExpectedType() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e");
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnList_ThatThrowsClassCast_WhenExceedMaxNulls_ButDidNotDetectBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g", 42);
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o);

                assertThat(list).isSameAs(o);
                assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
                    // We must do the assignment to get the ClassCastException
                    @SuppressWarnings("unused") String last = KiwiLists.last(list);
                });
            }

            @Test
            void shouldThrowTypeMismatchException_WhenIsNotCollection() {
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToListAndCheckElements(String.class, "not a collection"))
                        .withMessage("Cannot cast value to type java.util.List")
                        .withCauseInstanceOf(ClassCastException.class);
            }

            @Test
            void shouldThrowTypeMismatchException_WhenFindBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, 1, null, 2, 3, 4, 5, 6);
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToListAndCheckElements(String.class, o))
                        .withMessage("Expected java.util.List to contain elements of type java.lang.String, but found element of type java.lang.Integer");
            }
        }

        @Nested
        class UsingStandardListCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnList_WhenIsNullOrEmpty(List<?> coll) {
                var strategy = KiwiCasts2.StandardListCheckStrategy.ofDefaults();
                List<String> stringList = KiwiCasts2.castToListAndCheckElements(String.class, coll, strategy);
                assertThat(stringList).isSameAs(coll);
            }

            @Test
            void shouldReturnList_WhenAllElementsAreNull() {
                Object o = Lists.newArrayList(null, null, null, null, null, null);
                var strategy = KiwiCasts2.StandardListCheckStrategy.ofDefaults();
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o, strategy);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnList_WhenExceedMaxNulls() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                var strategy = KiwiCasts2.StandardListCheckStrategy.ofDefaults();
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o, strategy);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnList_WhenContainsExpectedType() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e");
                var strategy = KiwiCasts2.StandardListCheckStrategy.ofDefaults();
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o, strategy);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnList_WhenFewerElementsThanMaxTypeChecks() {
                Object o = Lists.newArrayList("a", "b", "c");
                var strategy = KiwiCasts2.StandardListCheckStrategy.of(5, 20);
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o, strategy);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnCollection_WhenMoreElementsThanMaxTypeChecks() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
                var strategy = KiwiCasts2.StandardListCheckStrategy.of(5, 15);
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o, strategy);
                assertThat(list).isSameAs(o);
            }

            @Test
            void shouldReturnList_ThatThrowsClassCast_WhenExceedMaxNulls_ButDidNotDetectBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", 42);
                var strategy = KiwiCasts2.StandardListCheckStrategy.of(3, 5);
                List<String> list = KiwiCasts2.castToListAndCheckElements(String.class, o, strategy);

                assertThat(list).isSameAs(o);
                assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
                    // We must do the assignment to get the ClassCastException
                    @SuppressWarnings("unused") String last = KiwiLists.last(list);
                });
            }

            @Test
            void shouldThrowTypeMismatchException_WhenIsNotList() {
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToListAndCheckElements(String.class, "not a list"))
                        .withMessage("Cannot cast value to type java.util.List")
                        .withCauseInstanceOf(ClassCastException.class);
            }

            @Test
            void shouldThrowTypeMismatchException_WhenFindBadType() {
                Object o = Lists.newArrayList(null, null, 1, null, 2, 3, 4, 5, 6);
                var strategy = KiwiCasts2.StandardListCheckStrategy.ofDefaults();
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToListAndCheckElements(String.class, o, strategy))
                        .withMessage("Expected java.util.List to contain elements of type java.lang.String, but found element of type java.lang.Integer");
            }
        }
    }

    @Nested
    class CastToSetAndCheckElements {

        @Nested
        class UsingDefaultSetCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnSet_WhenIsNullOrEmpty(Set<?> coll) {
                Set<String> stringSet = KiwiCasts2.castToSetAndCheckElements(String.class, coll);
                assertThat(stringSet).isSameAs(coll);
            }

            @Test
            void shouldReturnSet_WhenAllElementsAreNull() {
                Object o = Sets.newHashSet(null, null, null, null, null, null);
                Set<String> set = KiwiCasts2.castToSetAndCheckElements(String.class, o);
                assertThat(set).isSameAs(o);
            }

            @Test
            void shouldReturnSet_WhenExceedMaxNulls() {
                Object o = Sets.newHashSet(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                Set<String> coll = KiwiCasts2.castToSetAndCheckElements(String.class, o);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnSet_WhenContainsExpectedType() {
                Object o = Sets.newHashSet("a", "b", "c", "d", "e");
                Set<String> coll = KiwiCasts2.castToSetAndCheckElements(String.class, o);
                assertThat(coll).isSameAs(o);
            }

            @Test
            void shouldReturnSet_ThatThrowsClassCast_WhenExceedMaxNulls_ButDidNotDetectBadType() {
                // Note: LinkedHashSet is used here to preserve the order of elements
                Object o = Sets.newLinkedHashSet(
                        Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g", 42)
                );
                Set<String> coll = KiwiCasts2.castToSetAndCheckElements(String.class, o);

                assertThat(coll).isSameAs(o);
                var list = coll.stream().toList();
                assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
                    // We must do the assignment to get the ClassCastException
                    @SuppressWarnings("unused") String last = KiwiLists.last(list);
                });
            }

            @Test
            void shouldThrowTypeMismatchException_WhenIsNotSet() {
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToSetAndCheckElements(String.class, "not a collection"))
                        .withMessage("Cannot cast value to type java.util.Set")
                        .withCauseInstanceOf(ClassCastException.class);
            }

            @Test
            void shouldThrowTypeMismatchException_WhenFindBadType() {
                Object o = Sets.newHashSet(null, null, null, null, null, null, 1, null, 2, 3, 4, 5, 6);
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToSetAndCheckElements(String.class, o))
                        .withMessage("Expected java.util.Set to contain elements of type java.lang.String, but found element of type java.lang.Integer");
            }
        }

        @Nested
        class UsingStandardSetCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void shouldReturnList_WhenIsNullOrEmpty(Set<?> set) {
                var strategy = KiwiCasts2.StandardSetCheckStrategy.ofDefaults();
                Set<String> stringSet = KiwiCasts2.castToSetAndCheckElements(String.class, set, strategy);
                assertThat(stringSet).isSameAs(set);
            }

            @Test
            void shouldReturnSet_WhenAllElementsAreNull() {
                Object o = Sets.newHashSet(null, null, null, null, null, null);
                var strategy = KiwiCasts2.StandardSetCheckStrategy.ofDefaults();
                Set<String> set = KiwiCasts2.castToSetAndCheckElements(String.class, o, strategy);
                assertThat(set).isSameAs(o);
            }

            @Test
            void shouldReturnSet_WhenExceedMaxNulls() {
                Object o = Sets.newHashSet(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                var strategy = KiwiCasts2.StandardSetCheckStrategy.ofDefaults();
                Set<String> set = KiwiCasts2.castToSetAndCheckElements(String.class, o, strategy);
                assertThat(set).isSameAs(o);
            }

            @Test
            void shouldReturnSet_WhenContainsExpectedType() {
                Object o = Sets.newHashSet("a", "b", "c", "d", "e");
                var strategy = KiwiCasts2.StandardSetCheckStrategy.ofDefaults();
                Set<String> set = KiwiCasts2.castToSetAndCheckElements(String.class, o, strategy);
                assertThat(set).isSameAs(o);
            }

            @Test
            void shouldReturnSet_WhenFewerElementsThanMaxTypeChecks() {
                Object o = Sets.newHashSet("a", "b", "c");
                var strategy = KiwiCasts2.StandardSetCheckStrategy.of(5, 20);
                Set<String> set = KiwiCasts2.castToSetAndCheckElements(String.class, o, strategy);
                assertThat(set).isSameAs(o);
            }

            @Test
            void shouldReturnSet_WhenMoreElementsThanMaxTypeChecks() {
                Object o = Sets.newHashSet("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
                var strategy = KiwiCasts2.StandardSetCheckStrategy.of(5, 15);
                Set<String> set = KiwiCasts2.castToSetAndCheckElements(String.class, o, strategy);
                assertThat(set).isSameAs(o);
            }

            @Test
            void shouldReturnSet_ThatThrowsClassCast_WhenExceedMaxNulls_ButDidNotDetectBadType() {
                // Note: LinkedHashSet is used here to preserve the order of elements
                Object o = Sets.newLinkedHashSet(Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", 42));

                // Note: Use zero for maxNonNullChecks since the Set will contain only one null value.
                // This ensures we return immediately and don't check the rest of the elements.'
                var strategy = KiwiCasts2.StandardSetCheckStrategy.of(0, 5);
                Set<String> set = KiwiCasts2.castToSetAndCheckElements(String.class, o, strategy);

                assertThat(set).isSameAs(o);
                var list = set.stream().toList();
                assertThatExceptionOfType(ClassCastException.class).isThrownBy(() -> {
                    // We must do the assignment to get the ClassCastException
                    @SuppressWarnings("unused") String last = KiwiLists.last(list);
                });
            }

            @Test
            void shouldThrowTypeMismatchException_WhenIsNotSet() {
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToSetAndCheckElements(String.class, "not a set"))
                        .withMessage("Cannot cast value to type java.util.Set")
                        .withCauseInstanceOf(ClassCastException.class);
            }

            @Test
            void shouldThrowTypeMismatchException_WhenFindBadType() {
                Object o = Sets.newHashSet(null, null, 1, null, 2, 3, 4, 5, 6);
                var strategy = KiwiCasts2.StandardSetCheckStrategy.ofDefaults();
                assertThatExceptionOfType(TypeMismatchException.class)
                        .isThrownBy(() -> KiwiCasts2.castToSetAndCheckElements(String.class, o, strategy))
                        .withMessage("Expected java.util.Set to contain elements of type java.lang.String, but found element of type java.lang.Integer");
            }
        }
    }

    @Nested
    class CastToMapAndCheckElements {

        @Nested
        class UsingDefaultMapCheckingStrategy {

        }
    }
}
