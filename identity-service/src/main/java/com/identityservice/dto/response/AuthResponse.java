package com.identityservice.dto.response;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        UserSummaryResponse user
) {}
