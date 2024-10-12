package org.kiwiproject.beta.base.jar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.jar.Manifest;

@DisplayName("ManifestLookupResult")
class ManifestLookupResultTest {

    private Manifest manifest;

    @BeforeEach
    void setUp() {
        manifest = new Manifest();
    }

    @ParameterizedTest
    @EnumSource(value = ManifestLookupStatus.class, names = "SUCCESS", mode = EnumSource.Mode.EXCLUDE)
    void shouldRequireNullValue_WhenLookupFails(ManifestLookupStatus lookupStatus) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ManifestLookupResult(lookupStatus, manifest, null, null))
                .withMessage("manifest must be null when lookup fails");
    }

    @Test
    void shouldRequireNullErrorAndErrorMessage_WhenLookupSucceeds() {
        assertAll(
                () -> assertThatIllegalArgumentException().isThrownBy(() ->
                        new ManifestLookupResult(ManifestLookupStatus.SUCCESS, manifest, new RuntimeException(), null)),

                () -> assertThatIllegalArgumentException().isThrownBy(() ->
                        new ManifestLookupResult(ManifestLookupStatus.SUCCESS, manifest, null, "an error"))
        );
    }

    @Test
    void shouldHaveManifest_WhenLookupSucceeds() {
        var lookupResult = new ManifestLookupResult(ManifestLookupStatus.SUCCESS, manifest, null, null);

        assertAll(
                () -> assertThat(lookupResult.succeeded()).isTrue(),
                () -> assertThat(lookupResult.failed()).isFalse(),
                () -> assertThat(lookupResult.manifest()).isSameAs(manifest),
                () -> assertThat(lookupResult.maybeManifest()).containsSame(manifest),
                () -> assertThat(lookupResult.error()).isNull(),
                () -> assertThat(lookupResult.errorMessage()).isNull()
        );
    }

    @Test
    void shouldNotHaveManifest_WhenLookupFails() {
        var lookupResult = new ManifestLookupResult(ManifestLookupStatus.FAILURE, null, null, null);

        assertAll(
                () -> assertThat(lookupResult.succeeded()).isFalse(),
                () -> assertThat(lookupResult.failed()).isTrue(),
                () -> assertThat(lookupResult.manifest()).isNull(),
                () -> assertThat(lookupResult.maybeManifest()).isEmpty(),
                () -> assertThat(lookupResult.error()).isNull(),
                () -> assertThat(lookupResult.errorMessage()).isNull()
        );
    }

    @Test
    void canHaveError_WhenLookupFails() {
        var error = new SecurityException("Access Denied!");

        var lookupResult = new ManifestLookupResult(ManifestLookupStatus.FAILURE, null, error, null);

        assertAll(
                () -> assertThat(lookupResult.succeeded()).isFalse(),
                () -> assertThat(lookupResult.failed()).isTrue(),
                () -> assertThat(lookupResult.manifest()).isNull(),
                () -> assertThat(lookupResult.maybeManifest()).isEmpty(),
                () -> assertThat(lookupResult.error()).isSameAs(error),
                () -> assertThat(lookupResult.errorMessage()).isNull()
        );
    }

    @Test
    void canHaveErrorMessage_WhenLookupFails() {
        var error = new SecurityException("Access Denied!");
        var errorMessage = "some error";

        var lookupResult = new ManifestLookupResult(ManifestLookupStatus.FAILURE, null, error, errorMessage);

        assertAll(
                () -> assertThat(lookupResult.succeeded()).isFalse(),
                () -> assertThat(lookupResult.failed()).isTrue(),
                () -> assertThat(lookupResult.manifest()).isNull(),
                () -> assertThat(lookupResult.maybeManifest()).isEmpty(),
                () -> assertThat(lookupResult.error()).isSameAs(error),
                () -> assertThat(lookupResult.errorMessage()).isEqualTo(errorMessage)
        );
    }
}
