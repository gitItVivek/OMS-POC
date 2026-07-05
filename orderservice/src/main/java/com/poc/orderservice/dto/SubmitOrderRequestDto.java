package com.poc.orderservice.dto;

import jakarta.validation.constraints.NotNull;

public record SubmitOrderRequestDto(
        @NotNull
        Long submittedBy
) { }
