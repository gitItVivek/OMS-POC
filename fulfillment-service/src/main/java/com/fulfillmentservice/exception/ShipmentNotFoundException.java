package com.fulfillmentservice.exception;

import java.util.UUID;

public class ShipmentNotFoundException extends RuntimeException {

    public ShipmentNotFoundException(UUID orderId) {
        super("Shipment not found for order: " + orderId);
    }
}
