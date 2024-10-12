package org.kiwiproject.beta.base.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@DisplayName("JarManifestHelper")
class JarManifestHelperTest {

    private JarManifestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new JarManifestHelper();
    }

    @Nested
    class GetMainAttributeValueFromClass {

        @Test
        void shouldReturnValue_WhenAttributeExists() {
            var lookupResult = helper.getMainAttributeValue(Test.class, "Implementation-Title");

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(AttributeLookupStatus.EXISTS),
                    () -> assertThat(lookupResult.value()).isEqualTo("junit-jupiter-api"),
                    () -> assertThat(lookupResult.error()).isNull()
            );
        }

        @Test
        void shouldReturnNull_WhenAttributeDoesNotExist() {
            var lookupResult = helper.getMainAttributeValue(Test.class, "Bogus-Attribute");

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(AttributeLookupStatus.DOES_NOT_EXIST),
                    () -> assertThat(lookupResult.value()).isNull(),
                    () -> assertThat(lookupResult.error()).isNull()
            );
        }

        @Test
        void shouldReturnNull_WhenFailure() {
            var lookupResult = helper.getMainAttributeValue(String.class, "Implementation-Title");

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(AttributeLookupStatus.FAILURE),
                    () -> assertThat(lookupResult.value()).isNull(),
                    () -> assertThat(lookupResult.error()).isInstanceOf(IllegalStateException.class)
            );
        }
    }

    @Nested
    class GetMainAttributeValueFromManifest {

        private Manifest manifest;
        private Attributes mainAttributes;

        @BeforeEach
        void setUp() {
            mainAttributes = new Attributes();

            manifest = mock(Manifest.class);
            when(manifest.getMainAttributes()).thenReturn(mainAttributes);
        }

        @Test
        void shouldGetAttributeThatExists() {
            var name = "Some-Attribute";
            var value = "the.value";
            mainAttributes.putValue(name, value);

            var lookupResult = helper.getMainAttributeValue(manifest, name);

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(AttributeLookupStatus.EXISTS),
                    () -> assertThat(lookupResult.value()).isEqualTo(value));
        }

        @Test
        void shouldGetAttributeThatDoesNotExist() {
            var lookupResult = helper.getMainAttributeValue(manifest, "Some-Attribute");

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(AttributeLookupStatus.DOES_NOT_EXIST),
                    () -> assertThat(lookupResult.value()).isNull());
        }
    }

    @Nested
    class GetMainAttributesFromClass {

        @Test
        void shouldReturnValues_WhenAbleToReadManifest() {
            var lookupResult = helper.getMainAttributes(Test.class);

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(AttributesLookupStatus.SUCCESS),
                    () -> assertThat(lookupResult.attributes()).isNotEmpty(),
                    () -> assertThat(lookupResult.error()).isNull()
            );
        }

        @Test
        void shouldReturnNullMap_WhenUnableToReadManifest() {
            var lookupResult = helper.getMainAttributes(String.class);

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(AttributesLookupStatus.FAILURE),
                    () -> assertThat(lookupResult.attributes()).isNull(),
                    () -> assertThat(lookupResult.error()).isNull()
            );
        }
    }

    @Nested
    class GetMainAttributesFromManifest {

        private Manifest manifest;
        private Attributes mainAttributes;

        @BeforeEach
        void setUp() {
            mainAttributes = new Attributes();

            manifest = mock(Manifest.class);
            when(manifest.getMainAttributes()).thenReturn(mainAttributes);
        }

        @Test
        void shouldGetMainAttributesAsMap() {
            mainAttributes.put(new Attributes.Name("Attribute-1"), "value-1");
            mainAttributes.put(new Attributes.Name("Attribute-2"), "value-2");
            mainAttributes.put(new Attributes.Name("Attribute-3"), "value-3");

            var mainAttributesMap = helper.getMainAttributes(manifest);
            assertThat(mainAttributesMap).containsExactlyInAnyOrderEntriesOf(Map.of(
                    "Attribute-1", "value-1",
                    "Attribute-2", "value-2",
                    "Attribute-3", "value-3"
            ));
        }

        @Test
        void shouldGetMainAttributesAsMap_WhenNoneExist() {
            var mainAttributesMap = helper.getMainAttributes(manifest);
            assertThat(mainAttributesMap).isEmpty();
        }
    }

    @Nested
    class GetManifestFromClass {

        @Test
        void shouldGetManifest() {
            var manifestOptional = helper.getManifest(Test.class);
            assertThat(manifestOptional).isPresent();
        }

        @Test
        void shouldReturnEmptyOptional_WhenCannotFindIt() {
            var manifestOptional = helper.getManifest(String.class);
            assertThat(manifestOptional).isEmpty();
        }
    }

    @Nested
    class GetManifestWithResultFromClass {

        @Test
        void shouldGetManifest() {
            var lookupResult = helper.getManifestWithResult(Test.class);

            assertAll(
                    () -> assertThat(lookupResult.succeeded()).isTrue(),
                    () -> assertThat(lookupResult.manifest()).isNotNull()
            );
        }

        @Test
        void shouldReturnEmptyOptional_WhenCannotFindIt() {
            var lookupResult = helper.getManifestWithResult(String.class);

            assertAll(
                    () -> assertThat(lookupResult.succeeded()).isFalse(),
                    () -> assertThat(lookupResult.manifest()).isNull()
            );
        }
    }

    @Nested
    class GetManifestFromURI {

        @Test
        void shouldGetManifest() throws URISyntaxException {
            var location = Test.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            var manifestOptional = helper.getManifest(location);
            assertThat(manifestOptional).isPresent();
        }

        @Test
        void shouldReturnEmptyOptional_WhenCannotGetIt() {
            var bogusURI = URI.create("file:///tmp/foo.jar");
            var manifestOptional = helper.getManifest(bogusURI);
            assertThat(manifestOptional).isEmpty();
        }
    }

    @Nested
    class GetManifestWithResultFromURI {

        @Test
        void shouldGetManifest() throws URISyntaxException {
            var location = Test.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            var lookupResult = helper.getManifestWithResult(location);

            assertAll(
                    () -> assertThat(lookupResult.succeeded()).isTrue(),
                    () -> assertThat(lookupResult.manifest()).isNotNull()
            );
        }

        @Test
        void shouldReturnEmptyOptional_WhenCannotFindIt() {
            var location = URI.create("file:///tmp/foo.jar");
            var lookupResult = helper.getManifestWithResult(location);

            assertAll(
                    () -> assertThat(lookupResult.succeeded()).isFalse(),
                    () -> assertThat(lookupResult.manifest()).isNull()
            );
        }
    }

}
