package com.poc.orderservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateOrderRequestDto(
        @NotNull
        Long updatedBy,

        String remarks,

        @NotEmpty
        @Valid
        List<UpdateOrderItemRequestDto> items
) { }
