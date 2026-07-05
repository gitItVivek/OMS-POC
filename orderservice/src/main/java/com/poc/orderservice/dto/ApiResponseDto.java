package com.poc.orderservice.dto;

public record ApiResponseDto(
        int statusCode,
        String statusMessage,
        String message,
        Object response
) { }