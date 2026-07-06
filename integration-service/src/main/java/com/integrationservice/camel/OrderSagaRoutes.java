package com.integrationservice.camel;

import com.integrationservice.kafka.OmsKafkaTopics;
import com.integrationservice.messaging.OrderConfirmedEvent;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import com.integrationservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderSagaRoutes extends RouteBuilder {

    private final SagaOrchestratorService sagaOrchestratorService;

    @Override
    public void configure() {
        from("kafka:" + OmsKafkaTopics.ORDER_CREATED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-order-created")
                .unmarshal().json(OrderCreatedEvent.class)
                .bean(sagaOrchestratorService, "onOrderCreated");

        from("kafka:" + OmsKafkaTopics.INVENTORY_RESERVED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-stock-reserved")
                .unmarshal().json(StockReservedEvent.class)
                .bean(sagaOrchestratorService, "onStockReserved");

        from("kafka:" + OmsKafkaTopics.INVENTORY_RESERVATION_FAILED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-stock-failed")
                .unmarshal().json(StockReservationFailedEvent.class)
                .bean(sagaOrchestratorService, "onStockReservationFailed");

        from("kafka:" + OmsKafkaTopics.ORDER_CONFIRMED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-order-confirmed")
                .unmarshal().json(OrderConfirmedEvent.class)
                .bean(sagaOrchestratorService, "onOrderConfirmed");

        from("kafka:" + OmsKafkaTopics.FULFILLMENT_SHIPMENT_UPDATED_EVENT + "?groupId=integration-saga")
                .routeId("saga-on-shipment-updated")
                .unmarshal().json(ShipmentUpdatedEvent.class)
                .bean(sagaOrchestratorService, "onShipmentUpdated");
    }
}
