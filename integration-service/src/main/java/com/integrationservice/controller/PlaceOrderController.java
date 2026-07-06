package com.integrationservice.controller;

import com.integrationservice.dto.PlaceOrderRequestDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.web.RequestAuthContext;
import com.integrationservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PlaceOrderController {

    private final SagaOrchestratorService sagaOrchestratorService;

    @PostMapping("/place-order")
    public ResponseEntity<PlaceOrderResponseDto> placeOrder(@RequestBody PlaceOrderRequestDto request) {
        PlaceOrderResponseDto response = sagaOrchestratorService.startPlaceOrder(
                RequestAuthContext.currentUserId(),
                request.getItems());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
