package com.inventoryservice.kafka;

public record KafkaPipelineTopics(
        String inventoryReservedEvent,
        String inventoryReservationFailedEvent) {

    public static final KafkaPipelineTopics SAGA = new KafkaPipelineTopics(
            OmsKafkaTopics.INVENTORY_RESERVED_EVENT,
            OmsKafkaTopics.INVENTORY_RESERVATION_FAILED_EVENT);

    public static final KafkaPipelineTopics CAMEL = new KafkaPipelineTopics(
            OmsKafkaTopics.CAMEL_INVENTORY_RESERVED_EVENT,
            OmsKafkaTopics.CAMEL_INVENTORY_RESERVATION_FAILED_EVENT);
}
