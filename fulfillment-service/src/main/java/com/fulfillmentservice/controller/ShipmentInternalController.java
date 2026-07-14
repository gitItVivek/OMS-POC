package com.fulfillmentservice.controller;

import com.fulfillmentservice.dto.ShipmentResponseDto;
import com.fulfillmentservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/shipments")
@RequiredArgsConstructor
public class ShipmentInternalController {

    private final ShipmentService shipmentService;

    @PostMapping("/{orderId}/fulfill-to-delivered")
    public ShipmentResponseDto fulfillToDelivered(@PathVariable UUID orderId) {
        return shipmentService.fulfillToDelivered(orderId);
    }
}
