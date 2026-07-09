package com.integrationservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integrationservice.kafka.OmsKafkaTopics;
import com.integrationservice.messaging.OrderConfirmedEvent;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.inbound.KafkaMessageDrivenChannelAdapter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Path C Kafka listeners (Spring Integration). Always active alongside thin Camel.
 * Orders tagged event_adapter=SPRING_INTEGRATION are handled; Camel-owned orders are ignored.
 */
@Configuration
@RequiredArgsConstructor
public class OrderSagaIntegrationFlows {

    private static final String CONSUMER_GROUP = "integration-saga-si";

    private final SpringIntegrationSagaEventBridge springIntegrationSagaEventBridge;
    private final ConsumerFactory<String, String> consumerFactory;
    private final ObjectMapper objectMapper;

    @Bean
    public IntegrationFlow sagaOnOrderCreatedFlow() {
        return kafkaEventFlow(OmsKafkaTopics.ORDER_CREATED_EVENT, OrderCreatedEvent.class, "onOrderCreated");
    }

    @Bean
    public IntegrationFlow sagaOnStockReservedFlow() {
        return kafkaEventFlow(OmsKafkaTopics.INVENTORY_RESERVED_EVENT, StockReservedEvent.class, "onStockReserved");
    }

    @Bean
    public IntegrationFlow sagaOnStockReservationFailedFlow() {
        return kafkaEventFlow(
                OmsKafkaTopics.INVENTORY_RESERVATION_FAILED_EVENT,
                StockReservationFailedEvent.class,
                "onStockReservationFailed");
    }

    @Bean
    public IntegrationFlow sagaOnOrderConfirmedFlow() {
        return kafkaEventFlow(OmsKafkaTopics.ORDER_CONFIRMED_EVENT, OrderConfirmedEvent.class, "onOrderConfirmed");
    }

    @Bean
    public IntegrationFlow sagaOnShipmentUpdatedFlow() {
        return kafkaEventFlow(
                OmsKafkaTopics.FULFILLMENT_SHIPMENT_UPDATED_EVENT,
                ShipmentUpdatedEvent.class,
                "onShipmentUpdated");
    }

    private IntegrationFlow kafkaEventFlow(String topic, Class<?> eventType, String handlerMethod) {
        ContainerProperties containerProperties = new ContainerProperties(topic);
        containerProperties.setGroupId(CONSUMER_GROUP);
        containerProperties.setAckMode(ContainerProperties.AckMode.RECORD);

        ConcurrentMessageListenerContainer<String, String> container =
                new ConcurrentMessageListenerContainer<>(consumerFactory, containerProperties);

        KafkaMessageDrivenChannelAdapter<String, String> adapter =
                new KafkaMessageDrivenChannelAdapter<>(container);

        return IntegrationFlow.from(adapter)
                .transform(String.class, payload -> readJson(payload, eventType))
                .handle(springIntegrationSagaEventBridge, handlerMethod)
                .get();
    }

    private Object readJson(String payload, Class<?> eventType) {
        try {
            return objectMapper.readValue(payload, eventType);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deserialize Kafka payload to " + eventType.getSimpleName(), ex);
        }
    }
}
