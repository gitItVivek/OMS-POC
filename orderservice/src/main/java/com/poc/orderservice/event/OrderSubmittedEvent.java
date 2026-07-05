package com.poc.orderservice.event;

import com.poc.orderservice.dto.OrderItemResponseDto;

import java.math.BigDecimal;
import java.util.List;

public record OrderSubmittedEvent(
        Long orderId,
        String orderNumber,
        Long customerId,
        String orderStatus,
        BigDecimal totalAmount,
        String currency,
        List<OrderItemResponseDto> items,
        Long submittedAt
) { }
