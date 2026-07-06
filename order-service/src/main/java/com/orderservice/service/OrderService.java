package com.orderservice.service;

import com.orderservice.dto.CreateOrderRequestDto;
import com.orderservice.dto.OrderResponseDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderResponseDto createOrder(CreateOrderRequestDto request);

    OrderResponseDto getOrder(UUID orderId);

    List<OrderResponseDto> getOrdersForUser(UUID userId);
}
