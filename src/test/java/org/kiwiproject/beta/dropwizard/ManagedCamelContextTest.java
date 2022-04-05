package org.kiwiproject.beta.dropwizard;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ManagedCamelContext")
class ManagedCamelContextTest {
    
    private ManagedCamelContext managedCamelContext;
    private CamelContext camelContext;

    @BeforeEach
    void setUp() throws Exception {
        camelContext = new DefaultCamelContext();
        camelContext.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                        .to("log:end?level=INFO");
            }
        });

        managedCamelContext = new ManagedCamelContext(camelContext);
    }

    @AfterEach
    void tearDown() {
        if (camelContext.getStatus().isStarted()) {
            camelContext.stop();
        }
    }

    @Test
    void startsAndStopsCamelContext() {
        managedCamelContext.start();
        assertThat(camelContext.getStatus().isStarted()).isTrue();

        managedCamelContext.stop();

        assertThat(camelContext.getStatus().isStopped()).isTrue();
    }
}
