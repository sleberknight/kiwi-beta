package org.kiwiproject.beta.test.jakarta.ws.rs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kiwiproject.beta.test.jersey.ws.rs.JakartaRestTestHelpers;

import java.util.List;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;

@DisplayName("JerseyTestHelpers")
class JakartaRestTestHelpersTest {

    record Order(String id, String customerId) {
    }

    @Test
    void shouldConvertOutboundToInboundResponse_ForClass() {
        var order = new Order("12345", "7890");
        var outboundResponse = Response.ok(order).build();

        assertThatThrownBy(() -> outboundResponse.readEntity(Order.class))
                .describedAs("precondition: outbound response readEntity(Class) fails")
                .isExactlyInstanceOf(IllegalStateException.class);

        var inboundResponse = JakartaRestTestHelpers.toInboundResponse(outboundResponse);

        var entity = inboundResponse.readEntity(Order.class);
        assertThat(entity).isSameAs(order);
    }

    @Test
    void shouldConvertOutboundToInboundResponse_ForGenericType() {
        var customerOrders = List.of(
            new Order("12345", "1"),
            new Order("23456", "1"),
            new Order("78901", "1")
        );

        var outboundResponse = Response.ok(customerOrders).build();

        var orderListType = new GenericType<List<Order>>() {
        };
        assertThatThrownBy(() -> outboundResponse.readEntity(orderListType))
                .describedAs("precondition: outbound response readEntity(GenericType) fails")
                .isExactlyInstanceOf(IllegalStateException.class);

        var inboundResponse = JakartaRestTestHelpers.toInboundResponse(outboundResponse);

        var entity = inboundResponse.readEntity(orderListType);
        assertThat(entity).isSameAs(customerOrders);
    }
}
