package com.integrationservice.kafka;

public final class OmsKafkaTopics {

    public static final String ORDER_CREATE_COMMAND = "oms.order.create.command";
    public static final String ORDER_CREATED_EVENT = "oms.order.created.event";
    public static final String ORDER_CONFIRM_COMMAND = "oms.order.confirm.command";
    public static final String ORDER_CONFIRMED_EVENT = "oms.order.confirmed.event";
    public static final String ORDER_CANCEL_COMMAND = "oms.order.cancel.command";
    public static final String ORDER_CANCELLED_EVENT = "oms.order.cancelled.event";
    public static final String INVENTORY_RESERVE_COMMAND = "oms.inventory.reserve.command";
    public static final String INVENTORY_RESERVED_EVENT = "oms.inventory.reserved.event";
    public static final String INVENTORY_RESERVATION_FAILED_EVENT = "oms.inventory.reservation-failed.event";
    public static final String INVENTORY_RELEASE_COMMAND = "oms.inventory.release.command";
    public static final String FULFILLMENT_START_COMMAND = "oms.fulfillment.start.command";
    public static final String FULFILLMENT_SHIPMENT_UPDATED_EVENT = "oms.fulfillment.shipment-updated.event";
    public static final String NOTIFICATION_SEND_COMMAND = "oms.notification.send.command";

    private OmsKafkaTopics() {
    }
}
