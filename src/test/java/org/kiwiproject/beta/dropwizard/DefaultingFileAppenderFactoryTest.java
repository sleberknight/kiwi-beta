package org.kiwiproject.beta.dropwizard;

import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.google.common.collect.Iterators;
import io.dropwizard.logging.common.DropwizardLayout;
import io.dropwizard.logging.common.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.common.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.TimeZone;

@DisplayName("DefaultingFileAppenderFactory")
class DefaultingFileAppenderFactoryTest {

    private AsyncAppenderBase<ILoggingEvent> appender;

    @AfterEach
    void tearDown() {
        if (nonNull(appender) && appender.isStarted()) {
            appender.stop();
        }
    }

    @Test
    void shouldProvideDefaultValues() {
        var factory = new DefaultingFileAppenderFactory<ILoggingEvent>();
        appender = buildAppender(factory, null);

        assertThat(factory.getCurrentLogFilename()).endsWith("/service.log");
        assertThat(factory.getArchivedLogFilenamePattern()).endsWith("/service-%d.log.gz");
        assertThat(factory.getLogFormat()).isEqualTo("%-5level [%date] [%thread] %logger{5}: %message%n");

        assertExactlyOneRollingFileAppender(appender);
    }

    @Test
    void shouldProvideDefaultValues_UsingCustomApplicationName() {
        var factory = new DefaultingFileAppenderFactory<ILoggingEvent>();
        appender = buildAppender(factory, "order-service");

        assertThat(factory.getCurrentLogFilename()).endsWith("/order-service.log");
        assertThat(factory.getArchivedLogFilenamePattern()).endsWith("/order-service-%d.log.gz");
        assertThat(factory.getLogFormat()).isEqualTo("%-5level [%date] [%thread] %logger{5}: %message%n");

        assertExactlyOneRollingFileAppender(appender);
    }

    @Test
    void shouldUseDropwizardLayoutIfConfigured() {
        var factory = new DefaultingFileAppenderFactory<ILoggingEvent>();
        factory.setUseDefaultDropwizardLogFormat(true);
        appender = buildAppender(factory, "shipping-service");

        assertThat(factory.getCurrentLogFilename()).endsWith("/shipping-service.log");
        assertThat(factory.getArchivedLogFilenamePattern()).endsWith("/shipping-service-%d.log.gz");
        var expectedPattern = new DropwizardLayout(new LoggerContext(), TimeZone.getTimeZone("UTC")).getPattern();
        assertThat(factory.getLogFormat()).isEqualTo(expectedPattern);

        assertExactlyOneRollingFileAppender(appender);
    }

    @Test
    void shouldAcceptCustomValues(@TempDir Path tempDirPath) {
        var tempDir = tempDirPath.toString();
        var currentLogFilename = Path.of(tempDir, "log/services/invoice-service/invoice-service.log").toString();
        var archivedLogFilenamePattern = Path.of(tempDir, "log/services/invoice-service/invoice-service-%d.log.gz").toString();
        var logFormat = "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx";

        var factory = new DefaultingFileAppenderFactory<ILoggingEvent>();
        factory.setCurrentLogFilename(currentLogFilename);
        factory.setArchivedLogFilenamePattern(archivedLogFilenamePattern);
        factory.setLogFormat(logFormat);

        appender = buildAppender(factory, "invoice-service");

        assertThat(factory.getCurrentLogFilename()).isEqualTo(currentLogFilename);
        assertThat(factory.getArchivedLogFilenamePattern()).isEqualTo(archivedLogFilenamePattern);
        assertThat(factory.getLogFormat()).isEqualTo(logFormat);

        assertExactlyOneRollingFileAppender(appender);
    }

    private void assertExactlyOneRollingFileAppender(AsyncAppenderBase<ILoggingEvent> appender) {
        var appenderIterator = appender.iteratorForAppenders();
        var fileAppender = Iterators.getOnlyElement(appenderIterator);
        assertThat(fileAppender).isExactlyInstanceOf(RollingFileAppender.class);
        assertThat(fileAppender.isStarted()).isTrue();
    }

    private static AsyncAppenderBase<ILoggingEvent> buildAppender(
            DefaultingFileAppenderFactory<ILoggingEvent> factory,
            String applicationName) {

        return (AsyncAppenderBase<ILoggingEvent>) factory.build(
                new LoggerContext(),
                applicationName,
                new DropwizardLayoutFactory(),
                new ThresholdLevelFilterFactory(),
                new AsyncLoggingEventAppenderFactory());
    }
}
