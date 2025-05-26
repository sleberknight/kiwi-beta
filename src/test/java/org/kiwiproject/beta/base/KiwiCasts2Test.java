package org.kiwiproject.beta.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.kiwiproject.collect.KiwiLists;

import java.util.Collection;

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

        }
    }

    @Nested
    class CastToSetAndCheckElements {

        @Nested
        class UsingDefaultListElementCheckingStrategy {

        }
    }

    @Nested
    class CastToMapAndCheckElements {

        @Nested
        class UsingDefaultListElementCheckingStrategy {

        }
    }
}
