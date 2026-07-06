package com.fulfillmentservice.kafka;

public record KafkaPipelineTopics(String fulfillmentShipmentUpdatedEvent) {

    public static final KafkaPipelineTopics SAGA = new KafkaPipelineTopics(
            OmsKafkaTopics.FULFILLMENT_SHIPMENT_UPDATED_EVENT);

    public static final KafkaPipelineTopics CAMEL = new KafkaPipelineTopics(
            OmsKafkaTopics.CAMEL_FULFILLMENT_SHIPMENT_UPDATED_EVENT);
}
