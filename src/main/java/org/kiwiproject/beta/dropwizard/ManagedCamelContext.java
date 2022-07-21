package org.kiwiproject.beta.dropwizard;

import static java.util.stream.Collectors.toList;
import static org.kiwiproject.base.KiwiPreconditions.requireNotBlank;
import static org.kiwiproject.base.KiwiPreconditions.requireNotNull;
import static org.kiwiproject.logging.LazyLogParameterSupplier.lazy;

import com.google.common.annotations.Beta;
import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;

/**
 * Provides a Dropwizard {@link Managed} to start and stop a
 * {@link CamelContext} as your application is started and
 * stopped, respectively.
 */
@Beta
@Slf4j
public class ManagedCamelContext implements Managed {

    private final String name;
    private final CamelContext camelContext;

    /**
     * Creates a new instance with a default name.
     */
    public ManagedCamelContext(CamelContext camelContext) {
        this("camel-context-" + System.currentTimeMillis(), camelContext);
    }

    /**
     * Creates a new instance with the given name to be used in log messages.
     */
    public ManagedCamelContext(String name, CamelContext camelContext) {
        this.name = requireNotBlank(name);
        this.camelContext = requireNotNull(camelContext);
    }

    @Override
    public void start() {
        LOG.info("Starting Camel context {}", name);
        camelContext.start();

        LOG.info("Started Camel context {} has {} routes. Route IDs: {}",
                name,
                lazy(camelContext::getRoutesSize),
                lazy(() -> camelContext.getRoutes().stream().map(Route::getId).collect(toList())));
    }

    @Override
    public void stop() {
        LOG.info("Stopping Camel context {}", name);
        camelContext.stop();
    }
}
