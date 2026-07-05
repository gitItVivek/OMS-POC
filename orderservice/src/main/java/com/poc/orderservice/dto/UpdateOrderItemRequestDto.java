package com.poc.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateOrderItemRequestDto(
        Long itemId,

        Long productId,

        String productName,

        BigDecimal unitPrice,

        @NotNull
        @Positive
        Integer quantity
) { }
