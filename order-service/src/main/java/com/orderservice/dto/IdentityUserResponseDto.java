package com.orderservice.dto;

import java.util.UUID;

public record IdentityUserResponseDto(
        UUID id,
        String email,
        String displayName,
        boolean isVerified) {
}
