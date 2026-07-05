package com.fulfillmentservice.service;

import com.fulfillmentservice.dto.ShipmentResponseDto;

import java.util.UUID;

public interface ShipmentService {

    ShipmentResponseDto getByOrderId(UUID orderId);

    ShipmentResponseDto advance(UUID orderId);
}
