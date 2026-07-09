package com.integrationservice.kafka;

public final class OmsCamelPipelineKafkaTopics {

    public static final String ORDER_CREATE_COMMAND = "oms.camel.order.create.command";
    public static final String ORDER_CREATED_EVENT = "oms.camel.order.created.event";
    public static final String ORDER_CONFIRM_COMMAND = "oms.camel.order.confirm.command";
    public static final String ORDER_CONFIRMED_EVENT = "oms.camel.order.confirmed.event";
    public static final String ORDER_CANCEL_COMMAND = "oms.camel.order.cancel.command";
    public static final String INVENTORY_RESERVE_COMMAND = "oms.camel.inventory.reserve.command";
    public static final String INVENTORY_RESERVED_EVENT = "oms.camel.inventory.reserved.event";
    public static final String INVENTORY_RESERVATION_FAILED_EVENT = "oms.camel.inventory.reservation-failed.event";
    public static final String INVENTORY_RELEASE_COMMAND = "oms.camel.inventory.release.command";
    public static final String FULFILLMENT_START_COMMAND = "oms.camel.fulfillment.start.command";
    public static final String FULFILLMENT_SHIPMENT_UPDATED_EVENT = "oms.camel.fulfillment.shipment-updated.event";

    public static final String CONSUMER_GROUP = "integration-camel-pipeline";

    private OmsCamelPipelineKafkaTopics() {
    }
}
