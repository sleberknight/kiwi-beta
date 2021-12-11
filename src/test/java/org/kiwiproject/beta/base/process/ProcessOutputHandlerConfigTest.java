package org.kiwiproject.beta.base.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwiproject.test.validation.ValidationTestHelper.assertOnePropertyViolation;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiwiproject.reflect.KiwiReflection;

@DisplayName("ProcessOutputHandlerConfig")
@ExtendWith(SoftAssertionsExtension.class)
class ProcessOutputHandlerConfigTest {

    private ProcessOutputHandlerConfig config;

    @BeforeEach
    void setUp() {
        config = new ProcessOutputHandlerConfig();
    }

    @Test
    void shouldHaveDefaultValues(SoftAssertions softly) {
        softly.assertThat(config.getThreadPoolSize()).isEqualTo(5);
        softly.assertThat(config.getBufferCapacity().toBytes()).isEqualTo(2048);
        softly.assertThat(config.getSleepTime().toSeconds()).isEqualTo(1);
    }

    @Test
    void shouldValidateRequiredProperties(SoftAssertions softly) {
        KiwiReflection.invokeMutatorMethodsWithNull(config);

        assertOnePropertyViolation(softly, config, "threadPoolSize");
        assertOnePropertyViolation(softly, config, "bufferCapacity");
        assertOnePropertyViolation(softly, config, "sleepTime");
    }

    @Test
    void shouldReturnBufferCapacityInBytes() {
        assertThat(config.bufferCapacityInBytes())
                .isEqualTo(ProcessOutputHandlerConfig.DEFAULT_BUFFER_SIZE_BYTES);
    }

    @Test
    void shouldReturnSleepTimeInMillis() {
        assertThat(config.sleepTimeInMillis())
                .isEqualTo(ProcessOutputHandlerConfig.DEFAULT_SLEEP_DURATION_MILLIS);
    }
}
