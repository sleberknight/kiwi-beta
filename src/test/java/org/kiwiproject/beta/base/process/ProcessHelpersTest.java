package org.kiwiproject.beta.base.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.process.ProcessHelper;
import org.kiwiproject.base.process.Processes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@DisplayName("ProcessHelpers")
class ProcessHelpersTest {

    private ProcessHelper processHelper;

    // Force the Processes class to perform its internal static initialization before we run any tests
    @BeforeAll
    static void beforeAll() throws InterruptedException {
        var process = Processes.launch("pwd");
        process.waitFor(250, TimeUnit.MILLISECONDS);
    }

    @Nested
    class Execute {

        @BeforeEach
        void setUp() {
            processHelper = new ProcessHelper();
        }

        @Test
        void shouldReadStdOut() {
            var command = List.of("echo", "foo bar baz");
            var processResult = ProcessHelpers.execute(processHelper, command, 1_000_000, TimeUnit.MICROSECONDS);

            assertThat(processResult.isTimedOut()).isFalse();
            assertThat(processResult.getTimeoutThresholdMillis()).isEqualTo(1000);
            assertThat(processResult.getExitCode()).hasValue(0);
            assertThat(processResult.isSuccessfulExit()).isTrue();
            assertThat(processResult.isNotSuccessfulExit()).isFalse();
            assertThat(processResult.getStdOutLines()).containsOnly("foo bar baz");
            assertThat(processResult.getStdErrLines()).isEmpty();
            assertThat(processResult.hasError()).isFalse();
            assertThat(processResult.getError()).isEmpty();
        }

        @Test
        void shouldReadStdErr() {
            var processResult = ProcessHelpers.execute(processHelper, List.of("cat", "foo"));

            assertThat(processResult.isTimedOut()).isFalse();
            assertThat(processResult.getTimeoutThresholdMillis()).isEqualTo(5000);  // default timeout
            assertThat(processResult.getExitCode()).hasValue(1);
            assertThat(processResult.isSuccessfulExit()).isFalse();
            assertThat(processResult.isNotSuccessfulExit()).isTrue();
            assertThat(processResult.getStdOutLines()).isEmpty();
            assertThat(processResult.getStdErrLines()).containsOnly("cat: foo: No such file or directory");
            assertThat(processResult.hasError()).isFalse();
            assertThat(processResult.getError()).isEmpty();
        }

        @Test
        void shouldHandleTimeoutsGracefully_WhenProcessTakesLongerThanTimeout() {
            // This should take WAY more than 10 milliseconds
            var command = List.of("ls", "-lAR", "/");
            var processResult = ProcessHelpers.execute(processHelper, command, Duration.ofMillis(10));

            assertThat(processResult.isTimedOut()).isTrue();
            assertThat(processResult.getTimeoutThresholdMillis()).isEqualTo(10);
            assertThat(processResult.getExitCode()).isEmpty();
            assertThat(processResult.isSuccessfulExit()).isFalse();
            assertThat(processResult.isNotSuccessfulExit()).isTrue();
            assertThat(processResult.getStdOutLines()).isEmpty();
            assertThat(processResult.getStdErrLines()).isEmpty();
            assertThat(processResult.hasError()).isTrue();
            assertThat(processResult.getError()).containsInstanceOf(TimeoutException.class);
        }

        @Test
        void shouldHandleTimeoutsGracefully_WhenProcessNeverExits() {
            // Executing cat with no args causes it to wait indefinitely for stdin
            var command = List.of("cat");
            var processResult = ProcessHelpers.execute(processHelper, command, Duration.ofMillis(100));

            assertThat(processResult.isTimedOut()).isTrue();
            assertThat(processResult.getTimeoutThresholdMillis()).isEqualTo(100);
            assertThat(processResult.getExitCode()).isEmpty();
            assertThat(processResult.isSuccessfulExit()).isFalse();
            assertThat(processResult.isNotSuccessfulExit()).isTrue();
            assertThat(processResult.getStdOutLines()).isEmpty();
            assertThat(processResult.getStdErrLines()).isEmpty();
            assertThat(processResult.hasError()).isTrue();
            assertThat(processResult.getError()).containsInstanceOf(TimeoutException.class);
        }

        @Test
        void shouldHandleProcessExceptionsGracefully() {
            var processHelperSpy = spy(new ProcessHelper());

            // Must use the doThrow/when form here because when/thenThrow form throws
            // the following exception from ProcessBuilder#start:
            // java.lang.ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0
            var message = "Cannot run program \"some\"; error=2, No such file or directory";
            doThrow(new UncheckedIOException(new IOException(message)))
                    .when(processHelperSpy)
                    .launch(anyList());

            // Trying to run a command that does not exist results in IOException
            var command = List.of("some", "command");
            var processResult = ProcessHelpers.execute(processHelperSpy, command, 250, TimeUnit.MILLISECONDS);

            assertThat(processResult.isTimedOut()).isFalse();
            assertThat(processResult.getTimeoutThresholdMillis()).isEqualTo(250);
            assertThat(processResult.getExitCode()).isEmpty();
            assertThat(processResult.isSuccessfulExit()).isFalse();
            assertThat(processResult.isNotSuccessfulExit()).isTrue();
            assertThat(processResult.getStdOutLines()).isEmpty();
            assertThat(processResult.getStdErrLines()).isEmpty();
            assertThat(processResult.hasError()).isTrue();
            assertThat(processResult.getError()).isPresent();

            var error = processResult.getError().orElseThrow();
            assertThat(error)
                    .isExactlyInstanceOf(UncheckedIOException.class)
                    .hasCauseExactlyInstanceOf(IOException.class)
                    .hasMessageContaining(message);
        }
    }
}
