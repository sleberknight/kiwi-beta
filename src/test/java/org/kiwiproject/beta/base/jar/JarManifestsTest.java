package org.kiwiproject.beta.base.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.test.junit.jupiter.ClearBoxTest;

import java.net.URI;
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
                    .withMessage("Unable to get manifest for %s", String.class);
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

        @ClearBoxTest
            // mocks the SecurityManager
        void shouldReturnEmptyOptional_WhenExceptionOccursFindingIt() {
            var originalSecurityManager = System.getSecurityManager();

            try {
                // Create a mock SecurityManager that prevents access to the ProtectionDomain
                var mockSecurityManager = mock(SecurityManager.class);
                doThrow(new SecurityException("Access Denied!"))
                        .when(mockSecurityManager)
                        .checkPermission(argThat(permission -> "getProtectionDomain".equals(permission.getName())));

                System.setSecurityManager(mockSecurityManager);

                var manifestOptional = JarManifests.getManifest(String.class);
                assertThat(manifestOptional).isEmpty();

            } finally {
                // Make sure we reset it!
                System.setSecurityManager(originalSecurityManager);
            }
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
