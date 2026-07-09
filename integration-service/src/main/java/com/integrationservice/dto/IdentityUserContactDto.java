package com.integrationservice.dto;

import java.util.UUID;

public record IdentityUserContactDto(
        UUID id,
        String email,
        String displayName) {
}
