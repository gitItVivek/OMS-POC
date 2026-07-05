package com.poc.orderservice.service;

import com.poc.orderservice.dto.CreateOrderRequestDto;
import com.poc.orderservice.dto.OrderResponseDto;

public interface OrderService {

    OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto);

    OrderResponseDto getOrderById(Long orderId);
}
