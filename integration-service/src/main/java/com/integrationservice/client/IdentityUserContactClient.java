package com.integrationservice.client;

import com.integrationservice.dto.IdentityUserContactDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityUserContactClient {

    private final RestClient identityRestClient;

    public IdentityUserContactDto getUserContact(UUID userId) {
        return identityRestClient.get()
                .uri("/api/internal/users/{userId}/contact", userId)
                .retrieve()
                .body(IdentityUserContactDto.class);
    }
}
