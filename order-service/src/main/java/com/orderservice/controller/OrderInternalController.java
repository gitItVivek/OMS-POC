package com.orderservice.controller;

import com.orderservice.dto.InternalCreateOrderRequestDto;
import com.orderservice.dto.OrderResponseDto;
import com.orderservice.service.OrderLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

    private final OrderLifecycleService orderLifecycleService;

    @PostMapping
    public OrderResponseDto createOrder(@RequestBody InternalCreateOrderRequestDto request) {
        return orderLifecycleService.createOrderForCustomer(request);
    }

    @PostMapping("/{orderId}/confirm")
    public OrderResponseDto confirmOrder(@PathVariable UUID orderId) {
        return orderLifecycleService.confirmOrder(orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderResponseDto cancelOrder(@PathVariable UUID orderId) {
        return orderLifecycleService.cancelOrder(orderId);
    }
}
