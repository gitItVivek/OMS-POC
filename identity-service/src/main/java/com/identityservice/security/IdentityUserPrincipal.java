package com.identityservice.security;

import java.util.UUID;

public record IdentityUserPrincipal(
        UUID userId,
        String email,
        String role,
        boolean verified) {}
