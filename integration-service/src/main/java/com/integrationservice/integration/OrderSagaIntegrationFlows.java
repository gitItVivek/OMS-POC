package com.integrationservice.integration;

import com.integrationservice.kafka.OmsKafkaTopics;
import com.integrationservice.messaging.OrderConfirmedEvent;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import com.integrationservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class OrderSagaIntegrationFlows {

    private static final String GROUP_ID = "integration-saga";

    private final SagaOrchestratorService sagaOrchestratorService;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, String> sagaConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public IntegrationFlow orderCreatedSagaFlow(ConsumerFactory<String, String> sagaConsumerFactory) {
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(sagaConsumerFactory, OmsKafkaTopics.ORDER_CREATED_EVENT))
                .transform(payload -> objectMapper.readValue((String) payload, OrderCreatedEvent.class))
                .handle(sagaOrchestratorService, "onOrderCreated")
                .get();
    }

    @Bean
    public IntegrationFlow stockReservedSagaFlow(ConsumerFactory<String, String> sagaConsumerFactory) {
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(sagaConsumerFactory, OmsKafkaTopics.INVENTORY_RESERVED_EVENT))
                .transform(payload -> objectMapper.readValue((String) payload, StockReservedEvent.class))
                .handle(sagaOrchestratorService, "onStockReserved")
                .get();
    }

    @Bean
    public IntegrationFlow stockReservationFailedSagaFlow(ConsumerFactory<String, String> sagaConsumerFactory) {
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(sagaConsumerFactory, OmsKafkaTopics.INVENTORY_RESERVATION_FAILED_EVENT))
                .transform(payload -> objectMapper.readValue((String) payload, StockReservationFailedEvent.class))
                .handle(sagaOrchestratorService, "onStockReservationFailed")
                .get();
    }

    @Bean
    public IntegrationFlow orderConfirmedSagaFlow(ConsumerFactory<String, String> sagaConsumerFactory) {
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(sagaConsumerFactory, OmsKafkaTopics.ORDER_CONFIRMED_EVENT))
                .transform(payload -> objectMapper.readValue((String) payload, OrderConfirmedEvent.class))
                .handle(sagaOrchestratorService, "onOrderConfirmed")
                .get();
    }

    @Bean
    public IntegrationFlow shipmentUpdatedSagaFlow(ConsumerFactory<String, String> sagaConsumerFactory) {
        return IntegrationFlow.from(Kafka.messageDrivenChannelAdapter(sagaConsumerFactory, OmsKafkaTopics.FULFILLMENT_SHIPMENT_UPDATED_EVENT))
                .transform(payload -> objectMapper.readValue((String) payload, ShipmentUpdatedEvent.class))
                .handle(sagaOrchestratorService, "onShipmentUpdated")
                .get();
    }
}
