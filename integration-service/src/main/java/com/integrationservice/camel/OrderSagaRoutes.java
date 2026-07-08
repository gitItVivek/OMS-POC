package com.integrationservice.camel;

import com.integrationservice.kafka.OmsKafkaTopics;
import com.integrationservice.messaging.OrderConfirmedEvent;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * Path A Kafka listeners (thin Camel). Always active alongside SI for concurrent Postman runs.
 * Orders tagged event_adapter=CAMEL are handled; SI-owned orders are ignored in the orchestrator.
 */
@Component
@RequiredArgsConstructor
public class OrderSagaRoutes extends RouteBuilder {

    private final CamelSagaEventBridge camelSagaEventBridge;

    @Override
    public void configure() {
        from("kafka:" + OmsKafkaTopics.ORDER_CREATED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-order-created")
                .unmarshal().json(OrderCreatedEvent.class)
                .bean(camelSagaEventBridge, "onOrderCreated");

        from("kafka:" + OmsKafkaTopics.INVENTORY_RESERVED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-stock-reserved")
                .unmarshal().json(StockReservedEvent.class)
                .bean(camelSagaEventBridge, "onStockReserved");

        from("kafka:" + OmsKafkaTopics.INVENTORY_RESERVATION_FAILED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-stock-failed")
                .unmarshal().json(StockReservationFailedEvent.class)
                .bean(camelSagaEventBridge, "onStockReservationFailed");

        from("kafka:" + OmsKafkaTopics.ORDER_CONFIRMED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-order-confirmed")
                .unmarshal().json(OrderConfirmedEvent.class)
                .bean(camelSagaEventBridge, "onOrderConfirmed");

        from("kafka:" + OmsKafkaTopics.FULFILLMENT_SHIPMENT_UPDATED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-shipment-updated")
                .unmarshal().json(ShipmentUpdatedEvent.class)
                .bean(camelSagaEventBridge, "onShipmentUpdated");
    }
}
