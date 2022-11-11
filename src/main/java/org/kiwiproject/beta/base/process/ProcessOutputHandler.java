package org.kiwiproject.beta.base.process;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.slf4j.Slf4j;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.kiwiproject.base.DefaultEnvironment;
import org.kiwiproject.base.KiwiEnvironment;

import java.io.Closeable;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * For a given {@link Process}, consume its standard or error output and send it to a {@link Consumer} for
 * processing. The main use case is when your code launches a process and needs to receive and handle the
 * standard output and/or error of that process. Each process is handled asynchronously via tasks submitted to
 * an {@link ExecutorService}.
 */
@Slf4j
@Beta
public class ProcessOutputHandler implements Closeable {

    private static final int CALLBACK_THREAD_POOL_SIZE = 2;

    private final ListeningExecutorService listeningExecutorService;
    private final ExecutorService executorService;
    private final ExecutorService callbackExecutorService;
    private final KiwiEnvironment environment;
    private final int bufferCapacityBytes;
    private final long sleepTimeMillis;

    /**
     * Create new instance.
     *
     * @param config the configuration to use
     * @see #ProcessOutputHandler(int, int, long)
     */
    public ProcessOutputHandler(ProcessOutputHandlerConfig config) {
        this(config.getThreadPoolSize(), config.bufferCapacityInBytes(), config.sleepTimeInMillis());
    }

    /**
     * Create new instance.
     *
     * @param threadPoolSize      the number of threads to use when handling process output
     * @param bufferCapacityBytes the size of the buffer that will be used when reading process output (in bytes)
     * @param sleepTimeMillis     the amount of time to sleep between reading output from the process (in milliseconds)
     * @see #ProcessOutputHandler(ProcessOutputHandlerConfig)
     */
    public ProcessOutputHandler(int threadPoolSize, int bufferCapacityBytes, long sleepTimeMillis) {
        this(
                Executors.newFixedThreadPool(threadPoolSize),
                Executors.newFixedThreadPool(CALLBACK_THREAD_POOL_SIZE),
                new DefaultEnvironment(),
                bufferCapacityBytes,
                sleepTimeMillis
        );
    }

    @VisibleForTesting
    ProcessOutputHandler(ExecutorService executorService,
                         ExecutorService callbackExecutorService,
                         KiwiEnvironment environment,
                         int bufferCapacityBytes,
                         long sleepTimeMillis) {
        this.executorService = executorService;
        this.listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
        this.callbackExecutorService = callbackExecutorService;
        this.environment = environment;
        this.bufferCapacityBytes = bufferCapacityBytes;
        this.sleepTimeMillis = sleepTimeMillis;
    }

    /**
     * The type of output from the process.
     */
    public enum ProcessOutputType {

        /**
         * Indicates standard output of a process.
         */
        STANDARD("standard"),

        /**
         * Indicates error output of a process.
         */
        ERROR("error");

        private final String description;

        ProcessOutputType(String description) {
            this.description = description;
        }
    }

    /**
     * The return type for the handler methods.
     */
    public enum Result {

        /**
         * Indicates the process output is being handled.
         */
        HANDLING,

        /**
         * Indicates that the process was not alive when a handler was called, so it was ignored.
         */
        IGNORE_DEAD_PROCESS
    }

    /**
     * Handle the standard output of the given process.
     * <p>
     * Note that the consumer will receive output in chunks up to the size of the buffer capacity.
     * Each chunk might only be a fraction of the buffer size.
     *
     * @param process        the process
     * @param outputConsumer a Consumer that will handle the standard output of the process
     * @return the Result
     * @see Process#getInputStream()
     */
    public Result handleStandardOutput(Process process, Consumer<String> outputConsumer) {
        return handle(process, ProcessOutputType.STANDARD, outputConsumer);
    }

    /**
     * Handle the error output of the given process.
     * <p>
     * Note that the consumer will receive output in chunks up to the size of the buffer capacity.
     * Each chunk might only be a fraction of the buffer size.
     *
     * @param process       the process
     * @param errorConsumer a Consumer that will handle the error output of the process
     * @return the Result
     * @see Process#getErrorStream()
     */
    public Result handleErrorOutput(Process process, Consumer<String> errorConsumer) {
        return handle(process, ProcessOutputType.ERROR, errorConsumer);
    }

    /**
     * Handle the output or error output of the given process.
     * <p>
     * Note that the consumer will receive output in chunks up to the size of the buffer capacity.
     * Each chunk might only be a fraction of the buffer size.
     *
     * @param process        the process
     * @param outputType     what type of process to handle
     * @param outputConsumer a Consumer that will handle the selected type of process output
     * @return the Result
     */
    public Result handle(Process process, ProcessOutputType outputType, Consumer<String> outputConsumer) {
        var pid = pidOf(process).orElse("[unknown]");
        var outputTypeDesc = outputType.description;

        if (!process.isAlive()) {
            LOG.warn("Process {} is dead-on-arrival, no {} output to read!", pid, outputTypeDesc);
            return Result.IGNORE_DEAD_PROCESS;
        }

        LOG.debug("Submit task to read {} output of process {}", outputTypeDesc, pid);
        var task = createTask(process, outputType, outputConsumer, pid, outputTypeDesc);
        var listenableFuture = listeningExecutorService.submit(task);
        addCompletionLoggingCallback(pid, listenableFuture);

        return Result.HANDLING;
    }

    private static Optional<String> pidOf(Process process) {
        try {
            return Optional.of(String.valueOf(process.pid()));
        } catch (UnsupportedOperationException e) {
            return Optional.empty();
        }
    }

    private Callable<Object> createTask(Process process,
                                        ProcessOutputType streamType,
                                        Consumer<String> outputConsumer,
                                        String pid,
                                        String outputTypeDesc) {

        return () -> {
            try (var inputStream = selectInputStream(process, streamType);
                 var channel = Channels.newChannel(inputStream)) {

                var buffer = ByteBuffer.allocate(bufferCapacityBytes);

                while (process.isAlive()) {
                    LOG.trace("Reading up to {} bytes from {} output from process {}",
                            bufferCapacityBytes, outputTypeDesc, pid);
                    var bytesRead = channel.read(buffer);
                    LOG.trace("Read {} byte(s) from {} output from process {}", bytesRead, outputTypeDesc, pid);

                    buffer.flip();
                    var outputData = readStringAsUTF8(buffer);

                    outputConsumer.accept(outputData);

                    buffer.compact();

                    environment.sleepQuietly(sleepTimeMillis, TimeUnit.MILLISECONDS);
                }

                LOG.debug("Process {} is dead. No more {} output to read", pid, outputTypeDesc);
            }

            return null;
        };
    }

    private static InputStream selectInputStream(Process process, ProcessOutputType streamType) {
        return streamType == ProcessOutputType.STANDARD ? process.getInputStream() : process.getErrorStream();
    }

    private static String readStringAsUTF8(ByteBuffer byteBuffer) {
        var bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void addCompletionLoggingCallback(String processId, ListenableFuture<?> listenableFuture) {
        var callback = new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable Object result) {
                LOG.info("Handler for process {} completed without error", processId);
            }

            @Override
            public void onFailure(@NonNull Throwable error) {
                LOG.error("Handler for process {} had an error", processId, error);
            }
        };

        Futures.addCallback(listenableFuture, callback, callbackExecutorService);
    }

    @Override
    public void close() {
        LOG.info("Shutdown executors NOW");
        executorService.shutdownNow();
        callbackExecutorService.shutdownNow();
    }
}
