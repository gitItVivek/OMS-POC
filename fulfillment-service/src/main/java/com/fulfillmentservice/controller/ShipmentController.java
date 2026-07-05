package com.fulfillmentservice.controller;

import com.fulfillmentservice.dto.ShipmentResponseDto;
import com.fulfillmentservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @GetMapping("/{orderId}")
    public ShipmentResponseDto getByOrderId(@PathVariable UUID orderId) {
        return shipmentService.getByOrderId(orderId);
    }

    @PostMapping("/{orderId}/advance")
    public ShipmentResponseDto advance(@PathVariable UUID orderId) {
        return shipmentService.advance(orderId);
    }
}
