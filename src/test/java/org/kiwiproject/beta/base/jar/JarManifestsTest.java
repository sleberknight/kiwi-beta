package org.kiwiproject.beta.base.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.beta.base.jar.JarManifests.ClassHolder;
import org.kiwiproject.test.junit.jupiter.ClearBoxTest;

import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.jar.Attributes;

@DisplayName("JarManifests")
class JarManifestsTest {

    private static final String VERSION_ATTRIBUTE_NAME = Attributes.Name.IMPLEMENTATION_VERSION.toString();
    private static final String BOGUS_ENTRY_NAME = "Bogus-Entry-Name";

    @Nested
    class GetMainAttributeValueOrThrow {

        @Test
        void shouldGetValueFromJarManifest() {
            var version = JarManifests.getMainAttributeValueOrThrow(Test.class, VERSION_ATTRIBUTE_NAME);
            assertThat(version).isNotEmpty();
        }

        @Test
        void shouldThrow_WhenCannotFindIt() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> JarManifests.getMainAttributeValueOrThrow(Test.class, BOGUS_ENTRY_NAME))
                    .withMessage("Unable to get value for main attribute %s for %s", BOGUS_ENTRY_NAME, Test.class);
        }
    }

    @Nested
    class GetMainAttributeValue {

        @Test
        void shouldGetValueFromJarManifest() {
            var version = JarManifests.getMainAttributeValue(Test.class, VERSION_ATTRIBUTE_NAME);
            assertThat(version).isNotEmpty();
        }

        @Test
        void shouldReturnEmptyOptional_WhenEntryIsNotPresent() {
            var value = JarManifests.getMainAttributeValue(Test.class, BOGUS_ENTRY_NAME);
            assertThat(value).isEmpty();
        }
    }

    @Nested
    class GetManifestOrThrow {


        @Test
        void shouldGetManifest() {
            var manifest = JarManifests.getManifestOrThrow(Nested.class);
            assertThat(manifest).isNotNull();
        }

        @Test
        void shouldThrow_WhenCannotFindIt() {
            assertThatIllegalStateException()
                    .isThrownBy(() -> JarManifests.getManifestOrThrow(String.class))
                    .withMessage("Unable to get manifest for %s", String.class)
                    .withNoCause();
        }

    }

    @Nested
    class GetMainAttributesAsMapOrThrow {

        @Test
        void shouldGetMapOfStringAttributes() {
            var attrMap = JarManifests.getMainAttributesAsMapOrThrow(DisplayName.class);
            assertThat(attrMap).isNotEmpty();
        }

    }

    @Nested
    class GetManifestFromClass {

        @Test
        void shouldGetManifest() {
            var manifestOptional = JarManifests.getManifest(Test.class);
            assertThat(manifestOptional).isPresent();
        }

        @Test
        void shouldReturnEmptyOptional_WhenCannotFindIt() {
            var manifestOptional = JarManifests.getManifest(String.class);
            assertThat(manifestOptional).isEmpty();
        }

        @ClearBoxTest("Calls non-public getManifest(ClassHolder) to test exception in ProtectionDomain")
        void shouldReturnEmptyOptional_WhenExceptionOccursFindingIt() {
            var classHolder = new ClassHolder(String.class) {
                @Override
                ProtectionDomain getProtectionDomain() {
                    throw new SecurityException("Access Denied!");
                }
            };

            var lookupResult = JarManifests.getManifest(classHolder);

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(ManifestLookupStatus.FAILURE),
                    () -> assertThat(lookupResult.manifest()).isNull(),
                    () -> assertThat(lookupResult.error()).isInstanceOf(SecurityException.class),
                    () -> assertThat(lookupResult.errorMessage()).isNull(),
                    () -> assertThat(lookupResult.error())
                            .isInstanceOf(SecurityException.class)
                            .hasMessage("Access Denied!")
            );
        }

        @ClearBoxTest("Calls non-public getManifest(ClassHolder) to test null location in CodeSource")
        void shouldReturnEmptyOptional_WhenCodeSource_ReturnsNullLocation() {
            var codeSource = new CodeSource(/* location */ null, new Certificate[0]);
            var protectionDomain = new ProtectionDomain(codeSource, null);
            var classHolder = new ClassHolder(String.class) {
                @Override
                ProtectionDomain getProtectionDomain() {
                    return protectionDomain;
                }
            };

            var lookupResult = JarManifests.getManifest(classHolder);

            assertAll(
                    () -> assertThat(lookupResult.lookupStatus()).isEqualTo(ManifestLookupStatus.FAILURE),
                    () -> assertThat(lookupResult.manifest()).isNull(),
                    () -> assertThat(lookupResult.error()).isNull(),
                    () -> assertThat(lookupResult.errorMessage())
                            .isEqualTo("The Location of the CodeSource was null. CodeSource: " + codeSource)
            );
        }
    }

    @Nested
    class GetManifestFromURI {

        @Test
        void shouldReturnEmptyOptional_WhenCannotGetIt() {
            var bogusURI = URI.create("file:///tmp/foo.jar");
            var manifestOptional = JarManifests.getManifest(bogusURI);
            assertThat(manifestOptional).isEmpty();
        }
    }

}
