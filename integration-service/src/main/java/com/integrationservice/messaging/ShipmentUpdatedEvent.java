package com.integrationservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentUpdatedEvent {

    private UUID orderId;
    private UUID shipmentId;
    private String status;
    private String trackingNumber;
}
