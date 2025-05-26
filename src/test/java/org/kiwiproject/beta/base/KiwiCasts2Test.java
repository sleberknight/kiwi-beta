package org.kiwiproject.beta.base;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junitpioneer.jupiter.ExpectedToFail;

import java.util.Collection;

@DisplayName("KiwiCasts2")
class KiwiCasts2Test {

    // TODO

    @Nested
    class CastToCollectionAndCheckElements {

        @Nested
        class UsingDefaultCollectionCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void testTEMP_giveUpNullAndEmpty(Collection<String> coll) {
                Object o = coll;
                Collection<String> stringColl = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                System.out.println("coll = " + stringColl);
            }

            @Test
            void testTEMP_giveUpAllNull() {
                Object o = Lists.newArrayList(null, null, null, null, null, null);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                System.out.println("coll = " + coll);
            }

            @Test
            void testTEMP_giveUpTooManyNulls() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                System.out.println("coll = " + coll);
            }

            @Test
            @ExpectedToFail(withExceptions = ClassCastException.class)
            void testTEMP_giveUpTooManyNulls_ButDidNotDetectBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g", 42);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                System.out.println("coll = " + coll);

                coll.forEach(str -> {
                    if (str != null) {
                        System.out.println(str + " has length " + str.length());
                    }
                });
            }

            @Test
            @ExpectedToFail(withExceptions = TypeMismatchException.class)
            void testTEMP_findBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, 1, null, 2, 3, 4, 5, 6);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                System.out.println("coll = " + coll);
            }

            @Test
            void testTEMP_ok() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o);
                System.out.println("coll = " + coll);
            }
        }

        @Nested
        class UsingStandardCollectionCheckStrategy {

            @ParameterizedTest
            @NullAndEmptySource
            void testTEMP_giveUpNullAndEmpty(Collection<String> coll) {
                Object o = coll;
                Collection<String> stringColl = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults());
                System.out.println("coll = " + stringColl);
            }

            @Test
            void testTEMP_giveUpAllNull() {
                Object o = Lists.newArrayList(null, null, null, null, null, null);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults());
                System.out.println("coll = " + coll);
            }

            @Test
            void testTEMP_giveUpTooManyNulls() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults());
                System.out.println("coll = " + coll);
            }

            @Test
            @ExpectedToFail(withExceptions = ClassCastException.class)
            void testTEMP_giveUpTooManyNulls_ButDidNotDetectBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", null, null, null, "e", null, "f", "g", 42);
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults());
                System.out.println("coll = " + coll);

                coll.forEach(str -> {
                    if (str != null) {
                        System.out.println(str + " has length " + str.length());
                    }
                });
            }

            @Test
            @ExpectedToFail(withExceptions = TypeMismatchException.class)
            void testTEMP_findBadType() {
                Object o = Lists.newArrayList(null, null, null, null, null, null, "a", null, "b", "c", "d", 42, "e");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults());
                System.out.println("coll = " + coll);
            }

            @Test
            void testTEMP_ok_fewerElementsThanMaxTypeChecks() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults());
                System.out.println("coll = " + coll);
            }

            @Test
            void testTEMP_ok_moreElementsThanMaxTypeChecks() {
                Object o = Lists.newArrayList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z");
                Collection<String> coll = KiwiCasts2.castToCollectionAndCheckElements(String.class, o, KiwiCasts2.StandardCollectionCheckStrategy.ofDefaults());
                System.out.println("coll = " + coll);
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
