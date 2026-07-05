package com.poc.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record OrderItemDto(
        @NotNull
        Long productId,

        @NotBlank
        String productName,

        @NotNull
        @Positive
        Integer quantity,

        @NotNull
        @Positive
        BigDecimal unitPrice
) { }