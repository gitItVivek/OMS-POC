package com.orderservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the {@link StockLookupGateway} interface is wired to the channels expected
 * by {@code StockLookupIntegrationConfig}. These are declarative/annotation driven, so
 * this guards against a channel name or timeout being changed on one side without the other.
 */
class StockLookupGatewayTest {

    @Test
    void stockLookupGateway_isAnInterface() {
        assertThat(StockLookupGateway.class.isInterface()).isTrue();
    }

    @Test
    void gateway_declaresExpectedErrorChannel() {
        MessagingGateway annotation = StockLookupGateway.class.getAnnotation(MessagingGateway.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.errorChannel()).isEqualTo("stockLookupErrorChannel");
    }

    @Test
    void lookupStock_declaresExpectedRequestAndReplyChannelsAndTimeout() throws NoSuchMethodException {
        Method method = StockLookupGateway.class.getMethod("lookupStock", List.class);
        Gateway annotation = method.getAnnotation(Gateway.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.requestChannel()).isEqualTo("stockLookupRequestChannel");
        assertThat(annotation.replyChannel()).isEqualTo("stockLookupReplyChannel");
        assertThat(annotation.replyTimeout()).isEqualTo(10000L);
    }

    @Test
    void lookupStock_returnsListOfOrderItemStockResult() throws NoSuchMethodException {
        Method method = StockLookupGateway.class.getMethod("lookupStock", List.class);

        assertThat(method.getReturnType()).isEqualTo(List.class);
    }
}