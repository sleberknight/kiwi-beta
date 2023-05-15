package org.kiwiproject.beta.reflect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.cartesian.CartesianTest;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@DisplayName("TypeInfo")
class TypeInfoTest {

    @Nested
    class Constructor {

        @Test
        void shouldNotAllowNullRawType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new TypeInfo(null, List.of()));
        }

        @Test
        void shouldNotAllowNullListOfGenericTypes() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new TypeInfo(String.class, null));
        }
    }

    @Nested
    class OfType {

        @Test
        void shouldNotAllowNullArgument() {
            // noinspection DataFlowIssue
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TypeInfo.ofType(null));
        }
    }

    @Nested
    class OfSimpleType {

        @Test
        void shouldNotAllowNullArgument() {
            // noinspection DataFlowIssue
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TypeInfo.ofSimpleType(null));
        }

        @Test
        void shouldNotAllowParameterizedTypeArgument() {
            var parameterizedType = mock(ParameterizedType.class);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TypeInfo.ofSimpleType(parameterizedType));
        }

        @ParameterizedTest
        @ValueSource(classes = {Boolean.class, Integer.class, Long.class, String.class})
        void shouldAlwaysHaveEmptyGenericTypes(Class<?> rawType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);
            assertAll(
                    () -> assertThat(typeInfo.getRawType()).isEqualTo(rawType),
                    () -> assertThat(typeInfo.getGenericTypes()).isEmpty());
        }
    }

    @Nested
    class OfParameterizedType {

        @Test
        void shouldNotAllowNullArgument() {
            // noinspection DataFlowIssue
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TypeInfo.ofParameterizedType(null));
        }

        /**
         * We should never see a null returned from {@link ParameterizedType#getActualTypeArguments()} but we could
         * see an empty array. Let's ensure we handle both "just in case".
         */
        @ParameterizedTest
        @NullAndEmptySource
        void shouldHandleNullAndEmptyActualTypeArguments(Type[] actualTypeArguments) {
            var type = mock(ParameterizedType.class);
            when(type.getRawType()).thenReturn(List.class);
            when(type.getActualTypeArguments()).thenReturn(actualTypeArguments);

            var typeInfo = TypeInfo.ofParameterizedType(type);

            assertAll(
                    () -> assertThat(typeInfo.getRawType()).isEqualTo(List.class),
                    () -> assertThat(typeInfo.getGenericTypes()).isEmpty(),
                    () -> assertThatIllegalStateException().isThrownBy(typeInfo::getOnlyGenericType),
                    () -> assertThat(typeInfo.isCollection()).isTrue(),
                    () -> assertThat(typeInfo.isMap()).isFalse(),
                    () -> assertThat(typeInfo.hasExactRawType(List.class)).isTrue());
        }
    }

    @Nested
    class IsCollection {

        @ParameterizedTest
        @ValueSource(classes = {
            Collection.class, Set.class, List.class, ArrayList.class, HashSet.class, TreeSet.class
        })
        void shouldBeTrue_WhenCanAssignRawType_ToCollection(Class<?> rawType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isCollection()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(classes = { Map.class, HashMap.class, TreeMap.class, LinkedHashMap.class })
        void shouldBeFalse_WhenCannotAssignRawType_ToCollection(Class<?> rawType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class, String.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isCollection()).isFalse();
        }
    }

    @Nested
    class IsCollectionOf {

        @Test
        void shouldNotAllowNullArgument() {
            var type = TypeInfo.ofSimpleType(String.class);

            assertThatIllegalArgumentException().isThrownBy(() -> type.isCollectionOf(null));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#collectionGenericTypeArguments")
        void shouldBeTrue_WhenIsCollection_OfGenericType(Class<?> rawType, Class<?> genericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { genericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isCollectionOf(genericType)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#collectionGenericTypeArguments")
        void shouldBeFalse_WhenIsNotCollection_OfGenericType(Class<?> rawType, Class<?> genericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { genericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isCollectionOf(Number.class)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(classes = {Boolean.class, Integer.class, Long.class, String.class})
        void shouldBeFalse_WhenIsSimpleType(Class<?> rawType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.isCollectionOf(Boolean.class)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenIsMap() {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(Map.class);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class, String.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isCollectionOf(String.class)).isFalse();
        }
    }

    @Nested
    class IsListOf {

        @Test
        void shouldNotAllowNullArgument() {
            var type = TypeInfo.ofSimpleType(Boolean.class);

            assertThatIllegalArgumentException().isThrownBy(() -> type.isListOf(null));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#listGenericTypeArguments")
        void shouldBeTrue_WhenIsList_OfGenericType(Class<?> rawType, Class<?> genericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { genericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isListOf(genericType)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#listGenericTypeArguments")
        void shouldBeFalse_WhenIsNotList_OfGenericType(Class<?> rawType, Class<?> genericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { genericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isListOf(Number.class)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(classes = {Boolean.class, Integer.class, Long.class, String.class})
        void shouldBeFalse_WhenIsSimpleType(Class<?> rawType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.isListOf(Boolean.class)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenIsMap() {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(Map.class);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class, String.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isListOf(String.class)).isFalse();
        }
    }

    @Nested
    class IsSetOf {

        @Test
        void shouldNotAllowNullArgument() {
            var type = TypeInfo.ofSimpleType(Long.class);

            assertThatIllegalArgumentException().isThrownBy(() -> type.isSetOf(null));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#setGenericTypeArguments")
        void shouldBeTrue_WhenIsList_OfGenericType(Class<?> rawType, Class<?> genericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { genericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isSetOf(genericType)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#setGenericTypeArguments")
        void shouldBeFalse_WhenIsNotList_OfGenericType(Class<?> rawType, Class<?> genericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { genericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isSetOf(Number.class)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(classes = {Boolean.class, Integer.class, Long.class, String.class})
        void shouldBeFalse_WhenIsSimpleType(Class<?> rawType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.isSetOf(Boolean.class)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenIsMap() {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(Map.class);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class, String.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isSetOf(String.class)).isFalse();
        }
    }

    @Nested
    class IsMap {

        @ParameterizedTest
        @ValueSource(classes = { Map.class, HashMap.class, TreeMap.class, LinkedHashMap.class })
        void shouldBeTrue_WhenCanAssignRawType_ToMap(Class<?> rawType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class, String.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isMap()).isTrue();
        }

        @ParameterizedTest
        @ValueSource(classes = {
            Collection.class, Set.class, List.class, ArrayList.class, HashSet.class, TreeSet.class
        })
        void shouldBeFalse_WhenCannotAssignRawType_ToMap(Class<?> rawType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Long.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isMap()).isFalse();
        }
    }

    @Nested
    class IsMapOf {

        @Test
        void shouldNotAllowNullKeyTypeArgument() {
            var type = TypeInfo.ofSimpleType(Double.class);

            assertThatIllegalArgumentException().isThrownBy(() -> type.isMapOf(null, Integer.class));
        }

        @Test
        void shouldNotAllowNullValueTypeArgument() {
            var type = TypeInfo.ofSimpleType(Double.class);

            assertThatIllegalArgumentException().isThrownBy(() -> type.isMapOf(String.class, null));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#mapGenericTypeArguments")
        void shouldBeTrue_WhenIsList_OfGenericType(Class<?> rawType, Class<?> keyGenericType, Class<?> valueGenericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { keyGenericType, valueGenericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isMapOf(keyGenericType, valueGenericType)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#mapGenericTypeArguments")
        void shouldBeFalse_WhenIsNotList_OfGenericType(Class<?> rawType, Class<?> keyGenericType, Class<?> valueGenericType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { keyGenericType, valueGenericType });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isMapOf(String.class, Object.class)).isFalse();
        }

        @ParameterizedTest
        @ValueSource(classes = {Boolean.class, Integer.class, Long.class, String.class})
        void shouldBeFalse_WhenIsSimpleType(Class<?> rawType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.isMapOf(String.class, Object.class)).isFalse();
        }

        @Test
        void shouldBeFalse_WhenIsCollection() {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(List.class);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isMapOf(String.class, Object.class)).isFalse();
        }
    }

    @Nested
    class HasExactRawType {

        @Test
        void shouldNotAllowNullArgument() {
            var typeInfo = TypeInfo.ofSimpleType(Long.class);

            // noinspection DataFlowIssue
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> typeInfo.hasExactRawType(null));
        }

        @ParameterizedTest
        @ValueSource(classes = { String.class, CharSequence.class, Long.class })
        void shouldBeTrue_WhenHas_EXACT_RawType(Class<?> rawType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.hasExactRawType(rawType)).isTrue();
        }

        @CartesianTest
        void shouldBeFalse_WhenDoesNotHave_EXACT_RawType(
                @CartesianTest.Values(classes = { ArrayList.class, HashSet.class, HashMap.class }) Class<?> rawType,
                @CartesianTest.Values(classes = { List.class, Set.class, Map.class }) Class<?> testType) {

            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Integer.class, String.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.hasExactRawType(testType)).isFalse();
        }
    }

    @Nested
    class HasRawTypeAssignableTo {

        @Test
        void shouldNotAllowNullArgument() {
            var typeInfo = TypeInfo.ofSimpleType(Instant.class);

            // noinspection DataFlowIssue
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> typeInfo.hasRawTypeAssignableTo(null));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#rawTypesThatAreAssignable")
        void shouldBeTrue_WhenRawType_CanBeAssignedToTestType(Class<?> rawType, Class<?> testType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.hasRawTypeAssignableTo(testType)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.TypeInfoTest#rawTypesThatAreNotAssignable")
        void shouldBeFalse_WhenRawType_CannotBeAssignedToTestType(Class<?> rawType, Class<?> testType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.hasRawTypeAssignableTo(testType)).isFalse();
        }
    }

    @Nested
    class GetOnlyGenericType {

        @ParameterizedTest
        @ValueSource(classes = { String.class, Long.class, Boolean.class })
        void shouldReturnOnlyGenericType_WhenThereIsExactlyOneValue(Class<?> genericType) {
            var typeInfo = new TypeInfo(Collection.class, List.of(genericType));

            assertThat(typeInfo.getOnlyGenericType()).isEqualTo(genericType);
        }

        @Test
        void shouldThrowIllegalStateException_WhenThereAreNoValues() {
            var typeInfo = new TypeInfo(Collection.class, List.of());

            assertThatIllegalStateException().isThrownBy(typeInfo::getOnlyGenericType);
        }

        @Test
        void shouldThrowIllegalStateException_WhenThereIsMoreThanOneValue() {
            var typeInfo = new TypeInfo(Map.class, List.of(String.class, Object.class));

            assertThatIllegalStateException().isThrownBy(typeInfo::getOnlyGenericType);
        }
    }

    @Nested
    class VisibleForTestingMethods {

        @Nested
        class GetClassForName {

            @Test
            void shouldReturnTheClassWhenFound() {
                var clazz = TypeInfo.getClassForName(Integer.class.getName());
                assertThat(clazz).isEqualTo(Integer.class);
            }

            @ParameterizedTest
            @ValueSource(strings = { "com.acme.GNDN", "com.acme.Foo", "CantFindMe" })
            void shouldThrowIllegalStateException_WhenTheClassIsNotFound(String typeName) {
                assertThatIllegalStateException()
                        .isThrownBy(() -> TypeInfo.getClassForName(typeName))
                        .withMessageContaining(typeName);
            }
        }
    }

    static Stream<Arguments> rawTypesThatAreAssignable() {
        return Stream.of(
            Arguments.of(ArrayList.class, List.class),
            Arguments.of(HashSet.class, Set.class),
            Arguments.of(HashMap.class, Map.class),
            Arguments.of(LinkedList.class, Collection.class),
            Arguments.of(LinkedList.class, LinkedList.class),
            Arguments.of(Collection.class, Collection.class),
            Arguments.of(String.class, CharSequence.class),
            Arguments.of(Integer.class, Number.class)
        );
    }

    static Stream<Arguments> rawTypesThatAreNotAssignable() {
        return Stream.of(
            Arguments.of(ArrayList.class, Map.class),
            Arguments.of(Set.class, HashSet.class),
            Arguments.of(Map.class, HashMap.class),
            Arguments.of(HashMap.class, Collection.class),
            Arguments.of(CharSequence.class, String.class),
            Arguments.of(String.class, Number.class)
        );
    }

    static Stream<Arguments> collectionGenericTypeArguments() {
        return Stream.of(
            Arguments.of(Collection.class, String.class),
            Arguments.of(Collection.class, Integer.class),
            Arguments.of(List.class, String.class),
            Arguments.of(ArrayList.class, Long.class),
            Arguments.of(Set.class, String.class),
            Arguments.of(HashSet.class, Integer.class)
        );
    }

    static Stream<Arguments> listGenericTypeArguments() {
        return Stream.of(
            Arguments.of(List.class, String.class),
            Arguments.of(LinkedList.class, Integer.class),
            Arguments.of(CopyOnWriteArrayList.class, String.class),
            Arguments.of(ArrayList.class, Long.class),
            Arguments.of(ImmutableList.class, String.class),
            Arguments.of(ArrayList.class, Boolean.class)
        );
    }

    static Stream<Arguments> setGenericTypeArguments() {
        return Stream.of(
            Arguments.of(Set.class, String.class),
            Arguments.of(LinkedHashSet.class, Integer.class),
            Arguments.of(HashSet.class, String.class),
            Arguments.of(ImmutableSet.class, Long.class),
            Arguments.of(TreeSet.class, String.class),
            Arguments.of(NavigableSet.class, Integer.class)
        );
    }

    static Stream<Arguments> mapGenericTypeArguments() {
        return Stream.of(
            Arguments.of(Map.class, String.class, Integer.class),
            Arguments.of(LinkedHashMap.class, Integer.class, String.class),
            Arguments.of(HashMap.class, String.class, Boolean.class),
            Arguments.of(ImmutableMap.class, Long.class, String.class),
            Arguments.of(TreeMap.class, String.class, String.class),
            Arguments.of(NavigableMap.class, Integer.class, Double.class)
        );
    }
}
