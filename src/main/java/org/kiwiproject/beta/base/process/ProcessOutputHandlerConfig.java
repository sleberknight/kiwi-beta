package org.kiwiproject.beta.base.process;

import com.google.common.annotations.Beta;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Configuration class that can be used in conjunction with {@link ProcessOutputHandler}.
 */
@Getter
@Setter
@ToString
@Beta
public class ProcessOutputHandlerConfig {

    public static final int DEFAULT_THREAD_POOL_SIZE = 5;

    public static final int DEFAULT_BUFFER_SIZE_BYTES = 2048;

    public static final long DEFAULT_SLEEP_DURATION_MILLIS = 1000;

    /**
     * The number of threads to use when handling process output.
     */
    @NotNull
    private Integer threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

    /**
     * The size of the buffer that will be used when reading process output.
     */
    @NotNull
    private DataSize bufferCapacity = DataSize.bytes(DEFAULT_BUFFER_SIZE_BYTES);

    /**
     * The amount of time to sleep between reading output from the process.
     */
    @NotNull
    private Duration sleepTime = Duration.milliseconds(DEFAULT_SLEEP_DURATION_MILLIS);

    public int bufferCapacityInBytes() {
        return Math.toIntExact(bufferCapacity.toBytes());
    }

    public long sleepTimeInMillis() {
        return sleepTime.toMilliseconds();
    }

}
