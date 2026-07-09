package com.integrationservice.client;

import com.integrationservice.dto.NotificationEligibilityDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationRateLimitClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${notification.service.base-url}")
    private String notificationServiceBaseUrl;

    public NotificationEligibilityDto checkEligibility(UUID customerId, String notificationType) {
        return restClientBuilder.baseUrl(notificationServiceBaseUrl).build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/internal/notifications/eligibility")
                        .queryParam("customerId", customerId)
                        .queryParam("notificationType", notificationType)
                        .build())
                .retrieve()
                .body(NotificationEligibilityDto.class);
    }
}
