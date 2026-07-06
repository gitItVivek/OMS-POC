package com.fulfillmentservice.kafka;

public final class OmsKafkaTopics {

    public static final String FULFILLMENT_START_COMMAND = "oms.fulfillment.start.command";
    public static final String FULFILLMENT_SHIPMENT_UPDATED_EVENT = "oms.fulfillment.shipment-updated.event";

    public static final String CAMEL_FULFILLMENT_START_COMMAND = "oms.camel.fulfillment.start.command";
    public static final String CAMEL_FULFILLMENT_SHIPMENT_UPDATED_EVENT = "oms.camel.fulfillment.shipment-updated.event";

    private OmsKafkaTopics() {
    }
}
