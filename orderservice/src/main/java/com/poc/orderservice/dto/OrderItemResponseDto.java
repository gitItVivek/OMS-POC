package com.poc.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
) { }
