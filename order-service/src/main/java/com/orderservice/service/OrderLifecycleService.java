package com.orderservice.service;

import com.orderservice.dto.InternalCreateOrderRequestDto;
import com.orderservice.dto.OrderResponseDto;

import java.util.UUID;

public interface OrderLifecycleService {

    OrderResponseDto createOrderForCustomer(InternalCreateOrderRequestDto request);

    OrderResponseDto confirmOrder(UUID orderId);

    OrderResponseDto cancelOrder(UUID orderId);
}
