package org.kiwiproject.beta.dropwizard;

import static java.util.stream.Collectors.toList;
import static org.kiwiproject.logging.LazyLogParameterSupplier.lazy;

import com.google.common.annotations.Beta;

import org.apache.camel.CamelContext;
import org.apache.camel.Route;

import io.dropwizard.lifecycle.Managed;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides a Dropwizard {@link Managed} to start and stop a
 * {@link CamelContext} as your application is started and
 * stooped, respectively.
 */
@Beta
@Slf4j
public class ManagedCamelContext implements Managed {
    
    private final CamelContext camelContext;

    public ManagedCamelContext(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    @Override
    public void start() {
        LOG.info("Starting Camel context");
        camelContext.start();

        LOG.info("Started Camel context has {} routes. Route IDs: {}",
            lazy(() -> camelContext.getRoutes().size()),
            lazy(() -> camelContext.getRoutes().stream().map(Route::getId).collect(toList())));
    }

    @Override
    public void stop() {
        LOG.info("Stopping Camel context");
        camelContext.stop();
    }
}
