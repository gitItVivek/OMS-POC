package com.fulfillmentservice.dto;

import com.fulfillmentservice.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDto {

    private UUID shipmentId;
    private UUID orderId;
    private ShipmentStatus status;
    private String carrier;
    private String trackingNumber;
    private Instant createdAt;
    private Instant updatedAt;
}
