package com.orderservice.dto;

public record IdentityUserResponseDto(
        java.util.UUID id,
        String email,
        String displayName,
        boolean isVerified
) {
}
