package com.fulfillmentservice.dto;

import com.fulfillmentservice.enums.ShipmentStatus;
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
public class ShipmentUpdatedEventDto {

    private UUID orderId;
    private UUID shipmentId;
    private ShipmentStatus status;
    private String trackingNumber;
}
