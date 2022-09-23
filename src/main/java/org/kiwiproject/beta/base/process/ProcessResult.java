package org.kiwiproject.beta.base.process;

import static java.util.Objects.nonNull;

import com.google.common.annotations.Beta;

import org.kiwiproject.base.process.Processes;

import java.util.List;
import java.util.Optional;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Represents the result of running a command line process. May include data written
 * to stdout or stderr. There will not be an exit code if the process timed out, nor
 * stdout or stderr. There may or may not be an error (represented as a Throwable).
 */
@Beta
@Getter
@Builder
public class ProcessResult {

    // TODO Factory methods that enforce invariants instead of builder?
    //  Could keep builder private and have the factory methods use it.

    @NonNull
    private final List<String> stdOutLines;

    @NonNull
    private final List<String> stdErrLines;

    @Getter(AccessLevel.NONE)
    private final Integer exitCode;

    private final boolean timedOut;
    private final int timeoutThresholdMillis;

    @Getter(AccessLevel.NONE)
    private final Throwable error;

    /**
     * Return true if exit code is zero, and false for any other value.
     */
    public static boolean isSuccessfulExitCode(int exitCode) {
        return Processes.isSuccessfulExitCode(exitCode);
    }

    /**
     * Return {@link Optional} containing the process exit code, or an empty Optional if the process timed out.
     */
    public Optional<Integer> getExitCode() {
        return Optional.ofNullable(exitCode);
    }

    /**
     * Return true if the exit code is zero, and false for any other value.
     */
    public boolean isSuccessfulExit() {
        return ProcessResult.isSuccessfulExitCode(getExitCode().orElse(1));
    }

    /**
     * Return true if the exit code is NOT zero, and false for any other value.
     */
    public boolean isNotSuccessfulExit() {
        return !isSuccessfulExit();
    }

    /**
     * Return true if this result contains an error.
     */
    public boolean hasError() {
        return nonNull(error);
    }

    /**
     * Return {@link Optional} containing the error, or an empty Optional if the process succeeded.
     */
    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }
}
