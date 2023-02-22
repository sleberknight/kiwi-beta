package org.kiwiproject.beta.dropwizard;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Getter;
import lombok.Setter;

import org.kiwiproject.jar.KiwiJars;

import java.io.File;
import java.util.Optional;
import java.util.TimeZone;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import io.dropwizard.logging.AppenderFactory;
import io.dropwizard.logging.DropwizardLayout;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

/**
 * A Dropwizard {@link AppenderFactory} implementation which extends {@link FileAppenderFactory} to provide default
 * values for the current log file name, archive log file name pattern, and log format if they are not explicitly
 * configured. Once the appender is built, these values can be retrieved from the factory instance using
 * {@link #getCurrentLogFilename()}, {@link #getArchivedLogFilenamePattern()}, and {@link #getLogFormat()}.
 * <p>
 * This factory provides the same properties available in {@link FileAppenderFactory} and one additional property
 * which allows you to easily use the default Dropwizard log format instead of the one provided here.
 * <p>
 * To use this in a Dropwizard application, the FQCN must be listed in a {@code META-INF/services/io.dropwizard.logging.AppenderFactory}
 * file. Dropwizard already provides this file with its own implementations (in the {@code dropwizard-logging} JAR).
 * This library also provides the same file with our implementation. These two files (and any others from other providers)
 * must all be combined so the resulting file contains all implementations. We generally use the
 * <a href="https://maven.apache.org/plugins/maven-shade-plugin/">Maven Shade Plugin</a> with the
 * <a href="https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html#ServicesResourceTransformer">ServicesResourceTransformer</a>
 * to combine these service files.
 *
 * @see AppenderFactory
 * @see FileAppenderFactory
 * @see RollingFileAppender
 */
@Getter
@Setter
@JsonTypeName("rolling")
public class DefaultingFileAppenderFactory<E extends DeferredProcessingAware> extends FileAppenderFactory<E> {

    private static final String DEFAULT_LOG_FORMAT = "%-5level [%date] [%thread] %logger{5}: %message%n";

    /**
     * If true, uses the default Dropwizard log format provided by {@link DropwizardLayout}.
     */
    private boolean useDefaultDropwizardLogFormat;

    @Override
    public Appender<E> build(LoggerContext context,
            String applicationName,
            LayoutFactory<E> layoutFactory,
            LevelFilterFactory<E> levelFilterFactory,
            AsyncAppenderFactory<E> asyncAppenderFactory) {

        var nonNullApplicationName = Optional.ofNullable(applicationName).orElse("service");

        if (isBlank(getCurrentLogFilename())) {
            setCurrentLogFilename(filePathFor(nonNullApplicationName));
        }

        if (isBlank(getArchivedLogFilenamePattern())) {
            setArchivedLogFilenamePattern(archiveFileNamePatternFor(nonNullApplicationName));
        }

        if (isBlank(getLogFormat())) {
            var logFormat = newLogFormat(isUseDefaultDropwizardLogFormat(), getTimeZone());
            setLogFormat(logFormat);
        }

        return super.build(context, applicationName, layoutFactory, levelFilterFactory, asyncAppenderFactory);
    }

    private static String filePathFor(String applicationName) {
        return logPathFor(applicationName, ".log");
    }

    private static String archiveFileNamePatternFor(String applicationName) {
        return logPathFor(applicationName, "-%d.log.gz");
    }

    private static String logPathFor(String applicationName, String suffix) {
        var path = KiwiJars.getDirectoryPath(DefaultingFileAppenderFactory.class).orElse("./");
        return String.join(File.separator, path, applicationName) + suffix;
    }

    private static String newLogFormat(boolean useDefaultDropwizardLogFormat, TimeZone timeZone) {
        if (useDefaultDropwizardLogFormat) {
            var dropwizardLayout = new DropwizardLayout(new LoggerContext(), timeZone);
            return dropwizardLayout.getPattern();
        }

        return DEFAULT_LOG_FORMAT;
    }
}
