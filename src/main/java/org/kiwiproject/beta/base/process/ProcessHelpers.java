package org.kiwiproject.beta.base.process;

import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotEmpty;
import static org.kiwiproject.collect.KiwiLists.last;

import com.google.common.annotations.Beta;
import com.google.common.primitives.Ints;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.kiwiproject.base.KiwiStrings;
import org.kiwiproject.base.process.ProcessHelper;
import org.kiwiproject.base.process.Processes;
import org.kiwiproject.collect.KiwiLists;
import org.kiwiproject.io.KiwiIO;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utilities to execute a command using a {@link ProcessHelper}.
 * <p>
 * These static methods could be considered for addition to kiwi's {@link ProcessHelper}.
 * They would be instance methods inside ProcessHelper. Static versions of the methods could also be
 * added to kiwi's Processes class (which contains only static utilities), and then the instance
 * methods in ProcessHelper would delegate, thereby providing two ways to use this. Using ProcessHelper
 * is more friendly to testing since it can easily be mocked.
 */
@Beta
@UtilityClass
@Slf4j
public class ProcessHelpers {

    private static final int DEFAULT_TIMEOUT_MILLIS = 5_000;

    // TODO consider overloads or variants that allow callers to provide a working directory.
    //  Maybe that should be a method that takes a "parameter object" containing the command,
    //  timeout (with a default), working directory, whether to collect standard out/err, and
    //  possibly even a ProcessBuilder for complete customization, etc. If a ProcessBuilder is
    //  specified, then that would be used to launch a Process, otherwise a ProcessHelper would
    //  be required.

    // TODO consider adding methods to kiwi's Processes and ProcessHelper to supply a ProcessBuilder
    //  to the launch method, which would allow complete customization of the process that we don't
    //  currently support (b/c we have never needed them). Then we can add more 'execute' methods here
    //  that accept ProcessBuilder instead of ProcessHelper.

    /**
     * Execute command with a timeout of 5 seconds.
     *
     * @implNote See the implementation note in {@link #execute(ProcessHelper, List, long, TimeUnit)}.
     */
    public static ProcessResult execute(ProcessHelper processHelper, List<String> command) {
        return execute(processHelper, command, DEFAULT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }

    /**
     * Execute command with the given timeout.
     *
     * @see #execute(ProcessHelper, List, long, TimeUnit)
     */
    public static ProcessResult execute(ProcessHelper processHelper,
                                        List<String> command,
                                        Duration timeout) {

        var timeoutNanos = timeout.toNanos();
        return execute(processHelper, command, timeoutNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Execute command with the given timeout.
     *
     * @implNote This uses {@link CompletableFuture} to ensure we time out even if the stdout
     * or stderr blocks, which according to the {@link Process} docs, can at least theoretically
     * happen. For example, if someone gives the command {@code ls -laR /} to list all files in
     * the filesystem, it will probably take quite a long time.
     */
    public static ProcessResult execute(ProcessHelper processHelper,
                                        List<String> command,
                                        long timeout,
                                        TimeUnit timeoutUnit) {

        var timeoutMillis = Ints.checkedCast(timeoutUnit.toMillis(timeout));

        return tryExecute(processHelper, command, timeoutMillis);
    }

    private static ProcessResult tryExecute(ProcessHelper processHelper, List<String> command, int timeoutMillis) {
        try {
            LOG.trace("Executing command with timeout of {} millis: {}", timeoutMillis, command);
            return CompletableFuture
                    .supplyAsync(() -> executeSync(processHelper, command, timeoutMillis))
                    .get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while executing command with timeout {} millis: {}", timeoutMillis, command, e);
            return processResultFromException(e, timeoutMillis);

        } catch (Exception e) {
            LOG.error("{} while executing command with timeout {} millis: {}",
                    e.getClass().getSimpleName(), timeoutMillis, command, e);
            return processResultFromException(e, timeoutMillis);
        }
    }

    private static ProcessResult executeSync(ProcessHelper processHelper, List<String> command, int timeoutMillis) {
        var process = processHelper.launch(command);

        var stdOut = KiwiIO.readLinesFromInputStreamOf(process);
        var stdErr = KiwiIO.readLinesFromErrorStreamOf(process);

        var exitCodeOptional = processHelper.waitForExit(process, timeoutMillis, TimeUnit.MILLISECONDS);

        return ProcessResult.builder()
                .exitCode(exitCodeOptional.orElse(null))
                .timedOut(exitCodeOptional.isEmpty())
                .timeoutThresholdMillis(timeoutMillis)
                .stdOutLines(stdOut)
                .stdErrLines(stdErr)
                .build();
    }

    private static ProcessResult processResultFromException(Exception ex, int timeoutMillis) {
        var timedOut = ex instanceof TimeoutException;

        // If ExecutionException, unwrap it to get the actual cause of the problem
        var error = (ex instanceof ExecutionException) ? ex.getCause() : ex;

        return ProcessResult.builder()
                .error(error)
                .timedOut(timedOut)
                .timeoutThresholdMillis(timeoutMillis)
                .stdOutLines(List.of())
                .stdErrLines(List.of())
                .build();
    }

    /**
     * Convenience method that splits the given {@code command} on spaces before passing
     * it to {@link Processes#launch(List)}.
     * <p>
     * <strong>Warning:</strong>
     * If a command argument contains spaces and needs to be quoted, you cannot
     * use this method. Instead, use {@link Processes#launch(List)} or {@link Processes#launch(String...)}.
     * The reason is that this method just splits on all spaces, so arguments with spaces
     * would be broken up, and the quotes would become part of the two separate arguments.
     * In other words, this method is not a command parser.
     *
     * @param command the command to execute
     * @return the new {@link Process}
     * @throws UncheckedIOException if anything goes wrong, for example, if the working directory does not exist
     */
    public static Process launchCommand(String command) {
        return launchCommand(null, command);
    }

    /**
     * Convenience method that splits the given {@code command} on spaces before passing
     * it to {@link Processes#launch(List)}. The command uses the given working directory.
     * <p>
     * <strong>Warning:</strong> See the warning in {@link #launchCommand(String)}.
     *
     * @param workingDirectory the working directory for the command
     * @param command the command to execute
     * @return the new {@link Process}
     * @throws UncheckedIOException if anything goes wrong, for example, if the working directory does not exist
     */
    public static Process launchCommand(@Nullable File workingDirectory, String command) {
        checkArgumentNotBlank(command, "command must not be blank");

        var commandList = KiwiStrings.splitToList(command);
        return Processes.launch(workingDirectory, commandList);
    }

    /**
     * Convenience method that splits a pipeline using "|" and then splits each
     * command on spaces.
     * <p>
     * <strong>Warning:</strong> The same limitations on command splitting on spaces
     * apply to this method, as described in {@link #launchCommand(String)}. For
     * similar reasons, nested pipelines won't work either.
     *
     * @param pipeline the pipeline command
     * @return the <strong>last</strong> {@link Process} in the pipeline
     * @throws UncheckedIOException if anything goes wrong, for example, if the working directory does not exist
     * @see ProcessBuilder#startPipeline
     */
    public static Process launchPipelineCommand(String pipeline) {
        return launchPipelineCommand(null, pipeline);
    }

    /**
     * Convenience method that splits a pipeline using "|" and then splits each
     * command on spaces. Each command in the pipeline uses the given working directory.
     * <p>
     * <strong>Warning:</strong> The same limitations on command splitting on spaces
     * apply to this method, as described in {@link #launchCommand(String)}. For
     * similar reasons, nested pipelines won't work either.
     *
     * @param workingDirectory the working directory for each command in the pipeline
     * @param pipeline the pipeline command
     * @return the <strong>last</strong> {@link Process} in the pipeline
     * @throws UncheckedIOException if anything goes wrong executing the command
     * @see ProcessBuilder#startPipeline
     */
    public static Process launchPipelineCommand(@Nullable File workingDirectory, String pipeline) {
        checkArgumentNotBlank(pipeline, "pipeline must not be blank");

        List<List<String>> pipelineCommands = KiwiStrings.splitToList(pipeline, '|')
                .stream()
                .map(KiwiStrings::splitToList)
                .toList();

        return launchPipeline(workingDirectory, pipelineCommands);
    }

    /**
     * Executes a pipeline of the given commands.
     *
     * @param commands the commands in the pipeline
     * @return the <strong>last</strong> {@link Process} in the pipeline
     * @throws UncheckedIOException if anything goes wrong executing the command
     * @see ProcessBuilder#startPipeline
     */
    public static Process launchPipeline(List<List<String>> commands) {
        return launchPipeline(null, commands);
    }

    /**
     * Executes a pipeline of the given commands.
     * <p>
     * Each command in the pipeline uses the given working directory.
     *
     * @param workingDirectory the working directory for each command in the pipeline
     * @param commands the commands in the pipeline
     * @return the <strong>last</strong> {@link Process} in the pipeline
     * @throws UncheckedIOException if anything goes wrong, for example, if the working directory does not exist
     * @see ProcessBuilder#startPipeline
     */
    public static Process launchPipeline(@Nullable File workingDirectory,  List<List<String>> commands) {
        checkArgumentNotEmpty(commands, "commands must not be empty");
        var procBuilders = commands.stream()
                .filter(KiwiLists::isNotNullOrEmpty)
                .map(command -> new ProcessBuilder(command).directory(workingDirectory))
                .toList();

        try {
            var procs = ProcessBuilder.startPipeline(procBuilders);
            return last(procs);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
