package com.orderservice.kafka;

public final class OmsKafkaTopics {

    public static final String ORDER_CREATE_COMMAND = "oms.order.create.command";
    public static final String ORDER_CREATED_EVENT = "oms.order.created.event";
    public static final String ORDER_CONFIRM_COMMAND = "oms.order.confirm.command";
    public static final String ORDER_CONFIRMED_EVENT = "oms.order.confirmed.event";
    public static final String ORDER_CANCEL_COMMAND = "oms.order.cancel.command";
    public static final String ORDER_CANCELLED_EVENT = "oms.order.cancelled.event";

    private OmsKafkaTopics() {
    }
}
