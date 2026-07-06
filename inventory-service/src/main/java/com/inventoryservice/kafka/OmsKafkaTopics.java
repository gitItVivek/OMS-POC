package com.inventoryservice.kafka;

public final class OmsKafkaTopics {

    public static final String INVENTORY_RESERVE_COMMAND = "oms.inventory.reserve.command";
    public static final String INVENTORY_RESERVED_EVENT = "oms.inventory.reserved.event";
    public static final String INVENTORY_RESERVATION_FAILED_EVENT = "oms.inventory.reservation-failed.event";
    public static final String INVENTORY_RELEASE_COMMAND = "oms.inventory.release.command";

    private OmsKafkaTopics() {
    }
}
