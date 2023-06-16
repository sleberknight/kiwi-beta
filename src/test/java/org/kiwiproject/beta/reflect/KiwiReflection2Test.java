package org.kiwiproject.beta.reflect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;

import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.kiwiproject.beta.annotation.AccessedViaReflection;
import org.kiwiproject.beta.reflect.KiwiReflection2.JavaAccessModifier;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

@DisplayName("KiwiReflection2")
class KiwiReflection2Test {

    @Nested
    class HasAccessorModifier {

        @ParameterizedTest
        @CsvSource({
                "publicMethod, PUBLIC, true",
                "publicMethod, PROTECTED, false",
                "publicMethod, PRIVATE, false",
                "publicMethod, PACKAGE_PRIVATE, false",

                "protectedMethod, PUBLIC, false",
                "protectedMethod, PROTECTED, true",
                "protectedMethod, PRIVATE, false",
                "protectedMethod, PACKAGE_PRIVATE, false",

                "privateMethod, PUBLIC, false",
                "privateMethod, PROTECTED, false",
                "privateMethod, PRIVATE, true",
                "privateMethod, PACKAGE_PRIVATE, false",

                "packagePrivateMethod, PUBLIC, false",
                "packagePrivateMethod, PROTECTED, false",
                "packagePrivateMethod, PRIVATE, false",
                "packagePrivateMethod, PACKAGE_PRIVATE, true",

        })
        void shouldCheckMethods(String methodName,
                                JavaAccessModifier accessModifier,
                                boolean expectedReturnValue) throws Exception {

            var method = MethodAccessTestClass.class.getDeclaredMethod(methodName);

            var result = KiwiReflection2.hasAccessModifier(method, accessModifier);
            assertThat(result).isEqualTo(expectedReturnValue);
        }

        @ParameterizedTest
        @CsvSource({
            "PublicClass, PUBLIC, true",
            "PublicClass, PROTECTED, false",
            "PublicClass, PRIVATE, false",
            "PublicClass, PACKAGE_PRIVATE, false",

           "ProtectedClass, PUBLIC, false",
           "ProtectedClass, PROTECTED, true",
           "ProtectedClass, PRIVATE, false",
           "ProtectedClass, PACKAGE_PRIVATE, false",

           "PrivateClass, PUBLIC, false",
           "PrivateClass, PROTECTED, false",
           "PrivateClass, PRIVATE, true",
           "PrivateClass, PACKAGE_PRIVATE, false",

           "PackagePrivateClass, PUBLIC, false",
           "PackagePrivateClass, PROTECTED, false",
           "PackagePrivateClass, PRIVATE, false",
           "PackagePrivateClass, PACKAGE_PRIVATE, true"
        })
        void shouldCheckClasses(String className,
                                JavaAccessModifier accessModifier,
                                boolean expectedReturnValue) throws Exception {

            var enclosingTestClassName = getClass().getEnclosingClass().getName();
            var clazz = Class.forName(enclosingTestClassName + "$" + className);

            var result = KiwiReflection2.hasAccessModifier(clazz, accessModifier);
            assertThat(result).isEqualTo(expectedReturnValue);
        }

        @ParameterizedTest
        @CsvSource({
            "PublicConstructorAccessTestClass, PUBLIC, true",
            "PublicConstructorAccessTestClass, PROTECTED, false",
            "PublicConstructorAccessTestClass, PRIVATE, false",
            "PublicConstructorAccessTestClass, PACKAGE_PRIVATE, false",

            "ProtectedConstructorAccessTestClass, PUBLIC, false",
            "ProtectedConstructorAccessTestClass, PROTECTED, true",
            "ProtectedConstructorAccessTestClass, PRIVATE, false",
            "ProtectedConstructorAccessTestClass, PACKAGE_PRIVATE, false",

            "PrivateConstructorAccessTestClass, PUBLIC, false",
            "PrivateConstructorAccessTestClass, PROTECTED, false",
            "PrivateConstructorAccessTestClass, PRIVATE, true",
            "PrivateConstructorAccessTestClass, PACKAGE_PRIVATE, false",

            "PackagePrivateConstructorAccessTestClass, PUBLIC, false",
            "PackagePrivateConstructorAccessTestClass, PROTECTED, false",
            "PackagePrivateConstructorAccessTestClass, PRIVATE, false",
            "PackagePrivateConstructorAccessTestClass, PACKAGE_PRIVATE, true"
        })
        void shouldCheckConstructors(String className,
                                     JavaAccessModifier accessModifier,
                                     boolean expectedReturnValue) throws Exception {

            var enclosingTestClassName = getClass().getEnclosingClass().getName();
            var clazz = Class.forName(enclosingTestClassName + "$" + className);
            var constructor = clazz.getDeclaredConstructor();

            var result = KiwiReflection2.hasAccessModifier(constructor, accessModifier);
            assertThat(result).isEqualTo(expectedReturnValue);
        }

        @ParameterizedTest
        @CsvSource({
            "publicField, PUBLIC, true",
            "publicField, PROTECTED, false",
            "publicField, PRIVATE, false",
            "publicField, PACKAGE_PRIVATE, false",

            "protectedField, PUBLIC, false",
            "protectedField, PROTECTED, true",
            "protectedField, PRIVATE, false",
            "protectedField, PACKAGE_PRIVATE, false",

            "privateField, PUBLIC, false",
            "privateField, PROTECTED, false",
            "privateField, PRIVATE, true",
            "privateField, PACKAGE_PRIVATE, false",

            "packagePrivateField, PUBLIC, false",
            "packagePrivateField, PROTECTED, false",
            "packagePrivateField, PRIVATE, false",
            "packagePrivateField, PACKAGE_PRIVATE, true",
        })
        void shouldCheckFields(String fieldName,
                               JavaAccessModifier accessModifier,
                               boolean expectedReturnValue) throws Exception {

            var field = FieldAccessTestClass.class.getDeclaredField(fieldName);

            var result = KiwiReflection2.hasAccessModifier(field, accessModifier);
            assertThat(result).isEqualTo(expectedReturnValue);
        }
    }

    @SuppressWarnings({"EmptyMethod", "unused"})
    @AccessedViaReflection("methods are accessed via reflection")
    public static class MethodAccessTestClass {
        public void publicMethod() {
        }

        protected void protectedMethod() {
        }

        private void privateMethod() {
        }

        void packagePrivateMethod() {
        }
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    public static class PublicClass {
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    protected static class ProtectedClass {
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    private static class PrivateClass {
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    static class PackagePrivateClass {
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    public static class PublicConstructorAccessTestClass {
        public PublicConstructorAccessTestClass() {
        }
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    public static class ProtectedConstructorAccessTestClass {
        protected ProtectedConstructorAccessTestClass() {
        }
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    public static class PrivateConstructorAccessTestClass {
        private PrivateConstructorAccessTestClass() {
        }
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection
    public static class PackagePrivateConstructorAccessTestClass {
        PackagePrivateConstructorAccessTestClass() {
        }
    }

    @SuppressWarnings("unused")
    @AccessedViaReflection("Fields are accessed via reflection")
    public static class FieldAccessTestClass {
        public String publicField;
        protected String protectedField;
        private String privateField;
        String packagePrivateField;
    }

    @Nested
    class TypeInformationOfField {

        @Test
        void shouldNotAllowNullArgument() {
            // noinspection DataFlowIssue
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiReflection2.typeInformationOf((Field) null));
        }

        @Nested
        class ForSimpleTypes {

            @Test
            void shouldDetectBooleans() throws Exception {
                var field = Person.class.getDeclaredField("alive");

                var typeInfo = KiwiReflection2.typeInformationOf(field);

                assertAll(
                        () -> assertThat(typeInfo.getRawType()).isEqualTo(Boolean.class),
                        () -> assertThat(typeInfo.getGenericTypes()).isEmpty(),
                        () -> assertThatIllegalStateException().isThrownBy(() -> typeInfo.getOnlyGenericType()),
                        () -> assertThat(typeInfo.isCollection()).isFalse(),
                        () -> assertThat(typeInfo.isMap()).isFalse(),
                        () -> assertThat(typeInfo.hasExactRawType(Boolean.class)).isTrue());
            }

            @Test
            void shouldDetectStrings() throws Exception {
                var field = Person.class.getDeclaredField("name");

                var typeInfo = KiwiReflection2.typeInformationOf(field);

                assertAll(
                        () -> assertThat(typeInfo.getRawType()).isEqualTo(String.class),
                        () -> assertThat(typeInfo.getGenericTypes()).isEmpty(),
                        () -> assertThatIllegalStateException().isThrownBy(() -> typeInfo.getOnlyGenericType()),
                        () -> assertThat(typeInfo.isCollection()).isFalse(),
                        () -> assertThat(typeInfo.isMap()).isFalse(),
                        () -> assertThat(typeInfo.hasExactRawType(String.class)).isTrue());
            }
        }

        @Nested
        class ForContainerTypes {

            @Test
            void shouldDetectGenericLists() throws Exception {
                var field = Person.class.getDeclaredField("nicknames");

                var typeInfo = KiwiReflection2.typeInformationOf(field);

                assertAll(
                        () -> assertThat(typeInfo.getRawType()).isEqualTo(List.class),
                        () -> assertThat(typeInfo.getGenericTypes()).containsExactly(String.class),
                        () -> assertThat(typeInfo.getOnlyGenericType()).isEqualTo(String.class),
                        () -> assertThat(typeInfo.isCollection()).isTrue(),
                        () -> assertThat(typeInfo.isMap()).isFalse(),
                        () -> assertThat(typeInfo.hasExactRawType(List.class)).isTrue());
            }

            @Test
            void shouldDetectRawListsEvenThoughYouShouldNotUseThem() throws Exception {
                var field = Raw.class.getDeclaredField("rawList");

                var typeInfo = KiwiReflection2.typeInformationOf(field);

                assertAll(
                        () -> assertThat(typeInfo.getRawType()).isEqualTo(List.class),
                        () -> assertThat(typeInfo.getGenericTypes()).isEmpty(),
                        () -> assertThatIllegalStateException().isThrownBy(() -> typeInfo.getOnlyGenericType()),
                        () -> assertThat(typeInfo.isCollection()).isTrue(),
                        () -> assertThat(typeInfo.isMap()).isFalse(),
                        () -> assertThat(typeInfo.hasExactRawType(List.class)).isTrue());
            }

            @Test
            void shouldDetectGenericMaps() throws Exception {
                var field = Person.class.getDeclaredField("emailAddresses");

                var typeInfo = KiwiReflection2.typeInformationOf(field);

                assertAll(
                    () -> assertThat(typeInfo.getRawType()).isEqualTo(Map.class),
                    () -> assertThat(typeInfo.getGenericTypes()).containsExactly(String.class, String.class),
                    () -> assertThatIllegalStateException().isThrownBy(() -> typeInfo.getOnlyGenericType()),
                    () -> assertThat(typeInfo.isCollection()).isFalse(),
                    () -> assertThat(typeInfo.isMap()).isTrue(),
                    () -> assertThat(typeInfo.hasExactRawType(Map.class)).isTrue()
                );
            }

            @Test
            void shouldDetectRawMapsEvenThoughYouShouldNotUseThem() throws Exception {
                var field = Raw.class.getDeclaredField("rawMap");

                var typeInfo = KiwiReflection2.typeInformationOf(field);

                assertAll(
                    () -> assertThat(typeInfo.getRawType()).isEqualTo(Map.class),
                    () -> assertThat(typeInfo.getGenericTypes()).isEmpty(),
                    () -> assertThatIllegalStateException().isThrownBy(() -> typeInfo.getOnlyGenericType()),
                    () -> assertThat(typeInfo.isCollection()).isFalse(),
                    () -> assertThat(typeInfo.isMap()).isTrue(),
                    () -> assertThat(typeInfo.hasExactRawType(Map.class)).isTrue()
                );
            }
        }
    }

    @Nested
    class TypeInformationOfType {

        @Test
        void shouldNotAllowNullArgument() {
            // noinspection DataFlowIssue
            assertThatIllegalArgumentException().isThrownBy(() -> KiwiReflection2.typeInformationOf((Type) null));
        }

        @Test
        void shouldDelegateToTypeInfo() throws Exception {
            var field = Person.class.getDeclaredField("name");

            Type type = field.getGenericType();
            var typeInfo = KiwiReflection2.typeInformationOf(type);

            assertThat(typeInfo).isEqualTo(TypeInfo.ofType(type));
        }
    }

    @Value
    public static class Person {
        String name;
        Boolean alive;
        List<String> nicknames;
        Map<String, String> emailAddresses;
    }

    @SuppressWarnings("rawtypes")
    static class Raw {
        List rawList;
        Map rawMap;
    }
}
