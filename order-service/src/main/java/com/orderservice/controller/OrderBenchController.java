package com.orderservice.controller;

import com.orderservice.dto.BenchStepResponseDto;
import com.orderservice.dto.OrderCancelCommandDto;
import com.orderservice.dto.OrderConfirmCommandDto;
import com.orderservice.dto.OrderCreateCommandDto;
import com.orderservice.service.OrderBenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/bench")
@RequiredArgsConstructor
public class OrderBenchController {

    private final OrderBenchService orderBenchService;

    @PostMapping("/orders")
    public BenchStepResponseDto createOrder(@RequestBody OrderCreateCommandDto command) {
        orderBenchService.createOrder(command);
        return BenchStepResponseDto.builder()
                .success(true)
                .step("CREATE_ORDER")
                .message("Order created without Kafka events")
                .build();
    }

    @PostMapping("/orders/{orderId}/confirm")
    public BenchStepResponseDto confirmOrder(@PathVariable UUID orderId) {
        orderBenchService.confirmOrder(OrderConfirmCommandDto.builder().orderId(orderId).build());
        return BenchStepResponseDto.builder()
                .success(true)
                .step("CONFIRM_ORDER")
                .message("Order confirmed")
                .build();
    }

    @PostMapping("/orders/{orderId}/cancel")
    public BenchStepResponseDto cancelOrder(@PathVariable UUID orderId, @RequestBody(required = false) OrderCancelCommandDto command) {
        String reason = command != null && command.getReason() != null ? command.getReason() : "Bench pipeline compensation";
        orderBenchService.cancelOrder(OrderCancelCommandDto.builder().orderId(orderId).reason(reason).build());
        return BenchStepResponseDto.builder()
                .success(true)
                .step("CANCEL_ORDER")
                .message("Order cancelled")
                .build();
    }

    @org.springframework.web.bind.annotation.GetMapping("/orders/{orderId}/status")
    public java.util.Map<String, String> orderStatus(@PathVariable UUID orderId) {
        return java.util.Map.of("orderId", orderId.toString(), "status", orderBenchService.getOrderStatus(orderId));
    }
}
