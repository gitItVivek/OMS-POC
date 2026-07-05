package com.poc.orderservice.service;

import com.poc.orderservice.dto.CreateOrderRequestDto;
import com.poc.orderservice.dto.OrderResponseDto;
import com.poc.orderservice.dto.OrderSearchRequestDto;
import org.springframework.data.domain.Page;

public interface OrderService {

    OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto);

    OrderResponseDto getOrderById(Long orderId);

    Page<OrderResponseDto> searchOrder(OrderSearchRequestDto orderSearchRequestDto);
}
