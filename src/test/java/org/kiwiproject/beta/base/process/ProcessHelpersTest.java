package org.kiwiproject.beta.base.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.kiwiproject.base.process.Processes.waitForExit;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.RetryingTest;
import org.kiwiproject.base.process.ProcessHelper;
import org.kiwiproject.base.process.Processes;
import org.kiwiproject.io.KiwiIO;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

        @RetryingTest(3)
        void shouldReadStdOut() {
            var command = List.of("echo", "foo bar baz");
            var processResult = ProcessHelpers.execute(processHelper, command, 1_000_000, TimeUnit.MICROSECONDS);

            assertThat(processResult.isTimedOut()).describedAs("timed out after 1 second").isFalse();
            assertThat(processResult.getTimeoutThresholdMillis()).isEqualTo(1000);
            assertThat(processResult.getExitCode()).hasValue(0);
            assertThat(processResult.isSuccessfulExit()).isTrue();
            assertThat(processResult.isNotSuccessfulExit()).isFalse();
            assertThat(processResult.getStdOutLines()).containsOnly("foo bar baz");
            assertThat(processResult.getStdErrLines()).isEmpty();
            assertThat(processResult.hasError()).isFalse();
            assertThat(processResult.getError()).isEmpty();
        }

        @RetryingTest(3)
        void shouldReadStdErr() {
            var processResult = ProcessHelpers.execute(processHelper, List.of("cat", "foo"));

            assertThat(processResult.isTimedOut()).describedAs("timed out after 5 seconds").isFalse();
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
            // Executing cat command with no args causes it to wait indefinitely for stdin
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

        @RetryingTest(3)
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
            var processResult = ProcessHelpers.execute(processHelperSpy, command, 1_500, TimeUnit.MILLISECONDS);

            assertThat(processResult.isTimedOut()).describedAs("timed out after 1.5 seconds").isFalse();
            assertThat(processResult.getTimeoutThresholdMillis()).isEqualTo(1_500);
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

    @Nested
    class LaunchCommand {

        @Test
        void shouldLaunchCommand() {
            var process = ProcessHelpers.launchCommand("echo foo bar baz");
            waitForExit(process);

            var output = KiwiIO.readInputStreamOf(process).stripTrailing();
            assertThat(output).isEqualTo("foo bar baz");
        }

        @Test
        void shouldLaunchCommand_WithWorkingDirectory(@TempDir Path tempDir) throws IOException {
            var dir = tempDir.toFile();

            var subdir = Path.of(dir.toString(), "subdir");
            Files.createDirectory(subdir);

            var process = ProcessHelpers.launchCommand(dir, "ls -1");
            waitForExit(process);

            var output = KiwiIO.readLinesFromInputStreamOf(process);
            assertThat(output).contains("subdir");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", " \r\n "})
        void shouldNotAllowBlankCommands(String command) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ProcessHelpers.launchCommand(command));
        }
    }

    @Nested
    class LaunchPipelineCommand {

        @Test
        void shouldLaunchRegularCommands() {
            var process = ProcessHelpers.launchPipelineCommand("echo foo bar baz");
            waitForExit(process);

            var output = KiwiIO.readInputStreamOf(process).stripTrailing();
            assertThat(output).isEqualTo("foo bar baz");
        }

        @DisabledOnOs(value = OS.MAC)
        @Test
        void shouldLaunchPipelineCommands() {
            var process = ProcessHelpers.launchPipelineCommand("echo -e foo\nbar\nbaz | sort");
            waitForExit(process);

            var output = KiwiIO.readLinesFromInputStreamOf(process);
            assertThat(output).containsExactly(
                "bar",
                "baz",
                "foo"
            );
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", " \r\n "})
        void shouldNotAllowBlankPipelines(String command) {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ProcessHelpers.launchPipelineCommand(command));
        }

        @Test
        void shouldLaunchPipelineCommands_WithWorkingDirectory(@TempDir Path tempDir) throws IOException {
            var dir = tempDir.toFile();

            var subdirA = Path.of(dir.toString(), "a_subdir");
            Files.createDirectory(subdirA);

            var subdirB = Path.of(dir.toString(), "b_subdir");
            Files.createDirectory(subdirB);

            var subdirC = Path.of(dir.toString(), "c_subdir");
            Files.createDirectory(subdirC);

            var process = ProcessHelpers.launchPipelineCommand(dir, "ls -1 | sort -r");
            waitForExit(process);

            var output = KiwiIO.readLinesFromInputStreamOf(process);
            assertThat(output).containsSequence("c_subdir", "b_subdir", "a_subdir");
        }
    }

    @Nested
    class LaunchPipeline {

        @DisabledOnOs(value = OS.MAC)
        @Test
        void shouldLaunchPipeline() {
            var commands = List.of(
                List.of("echo", "-e", "foo foo\nbar bar\nbaz baz"),
                List.of("sort")
            );
            var process = ProcessHelpers.launchPipeline(commands);
            waitForExit(process);

            var output = KiwiIO.readLinesFromInputStreamOf(process);
            assertThat(output).containsExactly(
                "bar bar",
                "baz baz",
                "foo foo"
            );
        }

        @Test
        void shouldLaunchPipeline_WithWorkingDirectory(@TempDir Path tempDir) throws IOException {
            var dir = tempDir.toFile();

            var subdirA = Path.of(dir.toString(), "d_subdir");
            Files.createDirectory(subdirA);

            var subdirB = Path.of(dir.toString(), "e_subdir");
            Files.createDirectory(subdirB);

            var subdirC = Path.of(dir.toString(), "f_subdir");
            Files.createDirectory(subdirC);

            var ls = List.of("ls", "-1");
            var sort = List.of("sort", "-r");

            var process = ProcessHelpers.launchPipeline(dir, List.of(ls, sort));
            waitForExit(process);

            var output = KiwiIO.readLinesFromInputStreamOf(process);
            assertThat(output).containsSequence("f_subdir", "e_subdir", "d_subdir");
        }

        @Test
        void shouldNotAllowEmptyPipeline() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ProcessHelpers.launchPipeline(List.of()));
        }

        @Test
        void shouldIgnoreEmptyCommandsWithinPipeline() {
            var pipeline = List.<List<String>>of(
                List.of(),
                List.of("ls", "-1"),
                List.of(),
                List.of("sort", "--reverse"),
                List.of()
            );

            var process = ProcessHelpers.launchPipeline(pipeline);
            waitForExit(process);

            assertThat(process.exitValue()).isZero();
        }

        @Test
        void shouldThrowUncheckedIOException_WhenInvalidWorkingDirectory() {
            var ls = List.of("ls", "-1");
            var sort = List.of("sort", "-r");
            var pipeline = List.of(ls, sort);

            var dir = new File("/foo/bar/baz" + System.currentTimeMillis());

            assertThatExceptionOfType(UncheckedIOException.class)
                    .isThrownBy(() -> ProcessHelpers.launchPipeline(dir, pipeline))
                    .havingCause()
                    .isExactlyInstanceOf(IOException.class);
        }
    }
}
