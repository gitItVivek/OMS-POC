package com.orderservice.security;

import java.util.UUID;

public record AuthenticatedUserPrincipal(
        UUID userId,
        String email,
        String role,
        boolean verified) {}
