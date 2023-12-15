package org.kiwiproject.beta.util.git;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("KiwiGit")
class KiwiGitTest {

    @Nested
    class LooksLikeShortGitHash {

        @ParameterizedTest
        @ValueSource(strings = {
            "6c25ed0",
            "16fe531d",
            "e9f58128c",
            "973f2d2c72",
            "7ac1fe6a472",
            "6069f26bea43",
                "bafcade",  // low probability (0.104%), but can happen (this is a commit in this repo)
            "5381228",  // higher probability than only letters (3.725%) (this is a commit in this repo)
            "427738254",  // ditto above even though 9 digits long (1.455%) (this is a commit in this repo)
            "aaaaaaa",  // very unlikely, but we'll still accept it for now
            "1111111"  // very unlikely, but we'll still accept it for now
        })
        void shouldBeTrue_WhenMatchesExpectedPattern(String value) {
            assertThat(KiwiGit.looksLikeShortGitHash(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "6",  // too short
            "6c",
            "6c2",
            "6c25",
            "6c25e",
            "6c25ed",
            "random words",
            "text6c25ed0",  // random text, followed by a short Git hash
            "6c25ed0text",  // short Git hash, followed by random text
            "6C25ed0",  // contains uppercase letter
            "6c25Ed0",  // contains uppercase letter
            "6c25eD0",  // contains uppercase letter
            "6C25ED0",  // contains uppercase letters
            "d3f4179bb9419",  // too long
            "8b7013b7e212f4",
            "67900f1d8ee3044",
            "6c25ed0c5320711ef05a7a7836db3754ecd4fa4d",  // full 40-character SHA-1, too long
            "16fe531d950a17874818939f7f80cf541b33ac63",
            "HEAD",  // branch name, not commit hash
            "main",
            "master"
        })
        void shouldBeFalse_WhenDoesNotMatchExpectedPattern(String value) {
            assertThat(KiwiGit.looksLikeShortGitHash(value)).isFalse();
        }
    }
}
