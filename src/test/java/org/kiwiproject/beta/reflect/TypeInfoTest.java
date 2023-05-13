package org.kiwiproject.beta.reflect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TypeInfo.ofType(null));
        }
    }

    @Nested
    class OfSimpleType {

        @Test
        void shouldNotAllowNullArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> TypeInfo.ofSimpleType(null));
        }

        @ParameterizedTest
        @ValueSource(classes = { Boolean.class, Integer.class, Long.class, String.class })
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
        @ValueSource(classes = { Collection.class, Set.class, List.class, ArrayList.class, HashSet.class,
                TreeSet.class })
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
        @ValueSource(classes = { Collection.class, Set.class, List.class, ArrayList.class, HashSet.class,
                TreeSet.class })
        void shouldBeFalse_WhenCannotAssignRawType_ToMap(Class<?> rawType) {
            var parameterizedType = mock(ParameterizedType.class);
            when(parameterizedType.getRawType()).thenReturn(rawType);
            when(parameterizedType.getActualTypeArguments()).thenReturn(new Type[] { Long.class });

            var typeInfo = TypeInfo.ofParameterizedType(parameterizedType);

            assertThat(typeInfo.isMap()).isFalse();
        }
    }

    @Nested
    class HasExactRawType {

        @Test
        void shouldNotAllowNullArgument() {
            var typeInfo = TypeInfo.ofSimpleType(Long.class);

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

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> typeInfo.hasRawTypeAssignableTo(null));
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.KiwiReflection2Test#rawTypesThatAreAssignable")
        void shouldBeTrue_WhenRawType_CanBeAssignedToTestType(Class<?> rawType, Class<?> testType) {
            var typeInfo = TypeInfo.ofSimpleType(rawType);

            assertThat(typeInfo.hasRawTypeAssignableTo(testType)).isTrue();
        }

        @ParameterizedTest
        @MethodSource("org.kiwiproject.beta.reflect.KiwiReflection2Test#rawTypesThatAreNotAssignable")
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
}
