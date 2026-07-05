package com.identityservice.dto.response;

import java.util.UUID;

public record UserContactResponse(
        UUID id,
        String email,
        String displayName
) {}
