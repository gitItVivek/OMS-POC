package com.poc.orderservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequestDto(
        @NotNull
        Long customerId,

        @NotBlank
        String orderNumber,

        @NotBlank
        String orderStatus,

        @NotNull
        @Positive
        BigDecimal totalAmount,

        @NotBlank
        String currency,

        String paymentStatus,

        @NotNull
        Long orderDate,

        @NotNull
        Long createdBy,

        @NotEmpty
        List<OrderItemDto> items
) { }
