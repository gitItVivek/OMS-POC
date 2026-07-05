package com.poc.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponseDto(
        Long id,
        String orderNumber,
        Long customerId,
        String orderStatus,
        BigDecimal totalAmount,
        String currency,
        String paymentStatus,
        Long orderDate,
        List<OrderItemResponseDto> items
) { }
