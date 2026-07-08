package com.integrationservice.camel;

import com.integrationservice.bench.BenchPlaceOrderRequest;
import com.integrationservice.bench.PlaceOrderPipelineProcessor;
import com.integrationservice.kafka.OmsCamelPipelineKafkaTopics;
import com.integrationservice.messaging.OrderConfirmCommand;
import com.integrationservice.messaging.OrderCreateCommand;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.OrderLineItem;
import com.integrationservice.messaging.ReserveStockCommand;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StartFulfillmentCommand;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaceOrderCamelPipelineRoutes extends RouteBuilder {

    private static final String KAFKA_OPTIONS = "?groupId=" + OmsCamelPipelineKafkaTopics.CONSUMER_GROUP;

    private final PlaceOrderPipelineProcessor processor;

    @Override
    public void configure() {
        // --- Entry: HTTP triggers in-VM direct, then first Kafka command ---
        // This is benchmark path B where Camel DSL acts as the orchestration brain.
        from("direct:bench-place-order")
                .routeId("camel-pipeline-entry")
                .inputType(BenchPlaceOrderRequest.class)
                .bean(processor, "initialize")
                .bean(processor, "buildCreateOrderCommand")
                .marshal().json(JsonLibrary.Jackson, OrderCreateCommand.class)
                .to("kafka:" + OmsCamelPipelineKafkaTopics.ORDER_CREATE_COMMAND)
                .bean(processor, "buildAcceptedResponse");

        // --- Kafka event routes: orchestration stays in Camel DSL (heavy Camel) ---
        // order.created -> validate items (split) -> reserve stock
        from("kafka:" + OmsCamelPipelineKafkaTopics.ORDER_CREATED_EVENT + KAFKA_OPTIONS)
                .routeId("camel-on-order-created")
                .unmarshal().json(JsonLibrary.Jackson, OrderCreatedEvent.class)
                .bean(processor, "attachPipelineRun")
                .bean(processor, "extractLineItems")
                .split(body()).parallelProcessing()
                    .bean(processor, "validateLineItem")
                .end()
                .bean(processor, "buildReserveCommand")
                .marshal().json(JsonLibrary.Jackson, ReserveStockCommand.class)
                .to("kafka:" + OmsCamelPipelineKafkaTopics.INVENTORY_RESERVE_COMMAND);

        // inventory.reserved -> confirm order
        from("kafka:" + OmsCamelPipelineKafkaTopics.INVENTORY_RESERVED_EVENT + KAFKA_OPTIONS)
                .routeId("camel-on-stock-reserved")
                .unmarshal().json(JsonLibrary.Jackson, StockReservedEvent.class)
                .bean(processor, "attachPipelineRunFromOrderId")
                .bean(processor, "buildConfirmCommand")
                .marshal().json(JsonLibrary.Jackson, OrderConfirmCommand.class)
                .to("kafka:" + OmsCamelPipelineKafkaTopics.ORDER_CONFIRM_COMMAND);

        // inventory.reservation-failed -> release stock + cancel order (DSL compensation branch)
        from("kafka:" + OmsCamelPipelineKafkaTopics.INVENTORY_RESERVATION_FAILED_EVENT + KAFKA_OPTIONS)
                .routeId("camel-on-stock-failed")
                .unmarshal().json(JsonLibrary.Jackson, StockReservationFailedEvent.class)
                .choice()
                    .when(body().isNotNull())
                        .bean(processor, "buildReleaseCommand")
                        .marshal().json(JsonLibrary.Jackson)
                        .to("kafka:" + OmsCamelPipelineKafkaTopics.INVENTORY_RELEASE_COMMAND)
                        .bean(processor, "buildCancelCommand")
                        .marshal().json(JsonLibrary.Jackson)
                        .to("kafka:" + OmsCamelPipelineKafkaTopics.ORDER_CANCEL_COMMAND)
                        .bean(processor, "markPipelineFailed")
                .end();

        // order.confirmed -> start fulfillment
        from("kafka:" + OmsCamelPipelineKafkaTopics.ORDER_CONFIRMED_EVENT + KAFKA_OPTIONS)
                .routeId("camel-on-order-confirmed")
                .unmarshal().json(JsonLibrary.Jackson, com.integrationservice.messaging.OrderConfirmedEvent.class)
                .bean(processor, "attachPipelineRunFromOrderId")
                .bean(processor, "buildFulfillmentCommand")
                .marshal().json(JsonLibrary.Jackson, StartFulfillmentCommand.class)
                .to("kafka:" + OmsCamelPipelineKafkaTopics.FULFILLMENT_START_COMMAND);

        // shipment.updated -> mark pipeline completed in pipeline_runs table
        from("kafka:" + OmsCamelPipelineKafkaTopics.FULFILLMENT_SHIPMENT_UPDATED_EVENT + KAFKA_OPTIONS)
                .routeId("camel-on-shipment-updated")
                .unmarshal().json(JsonLibrary.Jackson, ShipmentUpdatedEvent.class)
                .bean(processor, "markPipelineCompleted");
    }
}
