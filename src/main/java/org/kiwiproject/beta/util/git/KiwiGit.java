package org.kiwiproject.beta.util.git;

import com.google.common.annotations.Beta;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
@Beta
public class KiwiGit {

    private static final Pattern SHORT_HASH_PATTERN = Pattern.compile("^[0-9a-f]{7,12}$");

    /**
     * Check whether the given input value looks like it is a short Git hash.
     * <p>
     * This method considers a short Git hash to be a 7-12 character long String containing only valid
     * hexadecimal characters. Specifically, valid hexadecimal characters include digits {@code 0-9}
     * and <em>lowercase</em> letters {@code a-f}. We only look for lowercase letters because we have
     * never once seen git display a commit hash with uppercase letters.
     * <p>
     * Read more about Git
     * <a href="https://git-scm.com/book/en/v2/Git-Tools-Revision-Selection#_short_sha_1">Short SHA-1 hashes</a>.
     *
     * @param value the String to check
     * @return true if the input String looks like a short Git hash
     */
    public static boolean looksLikeShortGitHash(String value) {
        return SHORT_HASH_PATTERN.matcher(value).matches();
    }
}
