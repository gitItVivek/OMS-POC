package com.integrationservice.controller;

import com.integrationservice.dto.PlaceOrderRequestDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.service.SyncPlaceOrderOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaceOrderController {

    private final SyncPlaceOrderOrchestrator syncPlaceOrderOrchestrator;

    @PostMapping("/place-order")
    public PlaceOrderResponseDto placeOrder(@RequestBody PlaceOrderRequestDto request) {
        return syncPlaceOrderOrchestrator.placeOrder(request);
    }
}
