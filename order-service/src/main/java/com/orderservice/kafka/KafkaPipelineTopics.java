package com.orderservice.kafka;

public record KafkaPipelineTopics(
        String orderCreatedEvent,
        String orderConfirmedEvent,
        String orderCancelledEvent) {

    public static final KafkaPipelineTopics SAGA = new KafkaPipelineTopics(
            OmsKafkaTopics.ORDER_CREATED_EVENT,
            OmsKafkaTopics.ORDER_CONFIRMED_EVENT,
            OmsKafkaTopics.ORDER_CANCELLED_EVENT);

    public static final KafkaPipelineTopics CAMEL = new KafkaPipelineTopics(
            OmsKafkaTopics.CAMEL_ORDER_CREATED_EVENT,
            OmsKafkaTopics.CAMEL_ORDER_CONFIRMED_EVENT,
            OmsKafkaTopics.CAMEL_ORDER_CANCELLED_EVENT);
}
