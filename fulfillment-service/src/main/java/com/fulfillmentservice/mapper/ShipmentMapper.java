package com.fulfillmentservice.mapper;

import com.fulfillmentservice.dto.ShipmentResponseDto;
import com.fulfillmentservice.entity.Shipment;
import org.springframework.stereotype.Component;

@Component
public class ShipmentMapper {

    public ShipmentResponseDto toResponseDto(Shipment shipment) {
        return ShipmentResponseDto.builder()
                .shipmentId(shipment.getId())
                .orderId(shipment.getOrderId())
                .status(shipment.getStatus())
                .carrier(shipment.getCarrier())
                .trackingNumber(shipment.getTrackingNumber())
                .createdAt(shipment.getCreatedAt())
                .updatedAt(shipment.getUpdatedAt())
                .build();
    }
}
