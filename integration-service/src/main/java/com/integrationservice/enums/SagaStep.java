package com.integrationservice.enums;

public enum SagaStep {
    ORDER_CREATE_SENT,
    INVENTORY_RESERVE_SENT,
    INVENTORY_RESERVED,
    ORDER_CONFIRM_SENT,
    COMPLETED,
    COMPENSATING,
    CANCELLED
}
