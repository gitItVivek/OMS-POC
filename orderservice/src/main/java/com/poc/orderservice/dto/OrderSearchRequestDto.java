package com.poc.orderservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderSearchRequestDto(
        Long customerId,

        String status,

        Long fromDate,

        Long toDate,

        @PositiveOrZero
        Integer page,

        @Positive
        @Max(100)
        Integer size,

        String sort
) { }
