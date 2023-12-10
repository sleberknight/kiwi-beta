package org.kiwiproject.beta.base.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_SECOND;
import static org.awaitility.Durations.TWO_SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.base.KiwiEnvironment;
import org.kiwiproject.beta.base.process.ProcessOutputHandler.Result;
import org.kiwiproject.io.KiwiIO;
import org.kiwiproject.util.function.KiwiConsumers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

@DisplayName("ProcessOutputHandler")
class ProcessOutputHandlerTest {

    private ProcessOutputHandler handler;
    private ExecutorService executorService;
    private ExecutorService callbackExecutorService;
    private KiwiEnvironment environment;
    private int bufferCapacityBytes;
    private long sleepTimeMillis;
    private Process process;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(1);
        callbackExecutorService = Executors.newFixedThreadPool(1);
        environment = mock(KiwiEnvironment.class);
        bufferCapacityBytes = 16;
        sleepTimeMillis = 2000;
        handler = new ProcessOutputHandler(
                executorService, callbackExecutorService, environment, bufferCapacityBytes, sleepTimeMillis);

        process = mock(Process.class);
        when(process.pid()).thenReturn(42L);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        shutdownExecutorsAndAwaitTermination();
    }

    @Test
    void shouldConstructFromConfig() {
        var config = new ProcessOutputHandlerConfig();
        config.setSleepTime(Duration.milliseconds(1));

        try (var customHandler = new ProcessOutputHandler(config)) {
            var result = customHandler.handleStandardOutput(process, KiwiConsumers.noOp());
            assertThat(result).isEqualTo(Result.IGNORE_DEAD_PROCESS);
        }
    }

    @Test
    void shouldCloseExecutors() {
        handler.close();

        await().atMost(TWO_SECONDS).until(() ->
                executorService.isShutdown() && callbackExecutorService.isShutdown());
    }

    @Test
    void shouldIgnoreDeadProcess() {
        expectProcessIsAliveTimes(0);

        var result = handler.handleStandardOutput(process, KiwiConsumers.noOp());

        assertThat(result).isEqualTo(Result.IGNORE_DEAD_PROCESS);

        verifyNoInteractions(environment);
    }

    @Test
    void shouldIgnoreWhenPidCannotBeRetrieved() {
        reset(process);
        when(process.pid()).thenThrow(new UnsupportedOperationException("no pid for you!"));

        var inputString = createTestInputRequiringThreeReads();
        var inputStream = KiwiIO.newByteArrayInputStreamOfLines(inputString);
        when(process.getInputStream()).thenReturn(inputStream);

        var numReads = 2;
        expectProcessIsAliveTimes(numReads + 1);

        var result = handler.handleStandardOutput(process, KiwiConsumers.noOp());
        assertThat(result).isEqualTo(Result.HANDLING);

        await().atMost(ONE_SECOND).until(() -> !process.isAlive());

        verify(environment, times(numReads)).sleepQuietly(sleepTimeMillis, TimeUnit.MILLISECONDS);
    }

    @Test
    void shouldConsumeNothing_WhenProcessIsAliveInitially_ButDeadOnceConsumingStarts() {
        var inputString = createTestInputRequiringThreeReads();
        var inputStream = KiwiIO.newByteArrayInputStreamOfLines(inputString);
        when(process.getInputStream()).thenReturn(inputStream);

        expectProcessIsAliveTimes(1);

        var builder = new StringBuilder();
        var result = handler.handleStandardOutput(process, builder::append);

        assertThat(result).isEqualTo(Result.HANDLING);

        await().atMost(ONE_SECOND).until(() -> !process.isAlive());

        assertThat(builder).isEmpty();

        verifyNoInteractions(environment);
    }

    @Test
    void shouldConsumeAllStandardOutput() {
        var numReads = 4;
        var inputString = createTestInputRequiringNumReads(numReads);
        var inputStream = KiwiIO.newByteArrayInputStreamOfLines(inputString);
        when(process.getInputStream()).thenReturn(inputStream);

        expectProcessIsAliveTimes(numReads + 1);

        var builder = new StringBuilder();
        var result = handler.handleStandardOutput(process, builder::append);

        assertThat(result).isEqualTo(Result.HANDLING);

        await().atMost(TWO_SECONDS).until(() -> !process.isAlive());

        assertThat(builder).hasToString(inputString);

        verify(environment, times(numReads)).sleepQuietly(sleepTimeMillis, TimeUnit.MILLISECONDS);
    }

    @Test
    void shouldConsumeAllErrorOutput() {
        var numReads = 5;
        var inputString = createTestInputRequiringNumReads(numReads);
        var inputStream = KiwiIO.newByteArrayInputStreamOfLines(inputString);
        when(process.getErrorStream()).thenReturn(inputStream);

        expectProcessIsAliveTimes(numReads + 1);

        var builder = new StringBuilder();
        var result = handler.handleErrorOutput(process, builder::append);

        assertThat(result).isEqualTo(Result.HANDLING);

        await().atMost(TWO_SECONDS).until(() -> !process.isAlive());

        assertThat(builder).hasToString(inputString);

        verify(environment, times(numReads)).sleepQuietly(sleepTimeMillis, TimeUnit.MILLISECONDS);
    }

    @Test
    void shouldConsumePartialOutput() {
        var numReads = 3;
        var inputString = createTestInputRequiringNumReads(numReads);
        var inputStream = KiwiIO.newByteArrayInputStreamOfLines(inputString);
        when(process.getInputStream()).thenReturn(inputStream);

        expectProcessIsAliveTimes(numReads);

        var builder = new StringBuilder();
        var result = handler.handleStandardOutput(process, builder::append);

        assertThat(result).isEqualTo(Result.HANDLING);

        await().atMost(TWO_SECONDS).until(() -> !process.isAlive());

        var bufferMultiplier = numReads - 1;
        var expectedNumCharsRead = bufferMultiplier * bufferCapacityBytes;
        assertThat(builder).hasToString(createTestString(expectedNumCharsRead));

        verify(environment, times(bufferMultiplier)).sleepQuietly(sleepTimeMillis, TimeUnit.MILLISECONDS);
    }

    @Test
    void shouldNotThrowTheExceptionWhenTaskFails() throws IOException {
        expectProcessIsAliveTimes(2);

        var ioException = new IOException("Warning! Warning! I/O error!");
        var inputStream = mock(InputStream.class);
        when(inputStream.read(any(), anyInt(), anyInt())).thenThrow(ioException);
        when(process.getErrorStream()).thenReturn(inputStream);

        var result = handler.handleErrorOutput(process, KiwiConsumers.noOp());

        assertThat(result).isEqualTo(Result.HANDLING);
    }

    private void expectProcessIsAliveTimes(int numTimesAlive) {
        if (numTimesAlive == 0) {
            when(process.isAlive()).thenReturn(false);
        } else if (numTimesAlive == 1) {
            when(process.isAlive()).thenReturn(true).thenReturn(false);
        } else {
            var ongoingStubbing = when(process.isAlive()).thenReturn(true);
            IntStream.rangeClosed(2, numTimesAlive).forEach(value -> ongoingStubbing.thenReturn(true));
            ongoingStubbing.thenReturn(false);
        }
    }

    private String createTestInputRequiringThreeReads() {
        return createTestInputRequiringNumReads(3);
    }

    private String createTestInputRequiringNumReads(int numReadsWanted) {
        // if caller wants N reads, then create a string with one more char than (N - 1) times the buffer capacity,
        // for example, if caller wants N=3 reads and buffer capacity is 10, then the string will be:
        // 1 + (3 - 1) * 10 = 21 characters long, and the reads will be 10 bytes, then 10 bytes, then 1 byte.

        var bufferMultiplier = numReadsWanted - 1;
        return createTestString(bufferCapacityBytes * bufferMultiplier + 1);
    }

    private static String createTestString(int numChars) {
        var chars = new byte[numChars];
        Arrays.fill(chars, (byte) 'a');
        return new String(chars, StandardCharsets.UTF_8);
    }

    private void shutdownExecutorsAndAwaitTermination() throws InterruptedException {
        shutdownExecutorAndAwaitTermination(executorService);
        shutdownExecutorAndAwaitTermination(callbackExecutorService);
    }

    private static void shutdownExecutorAndAwaitTermination(ExecutorService executor) throws InterruptedException {
        executor.shutdown();

        // noinspection ResultOfMethodCallIgnored
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
