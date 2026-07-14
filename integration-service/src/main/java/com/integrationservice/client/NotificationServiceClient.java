package com.integrationservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class NotificationServiceClient {

    private final RestClient restClient;

    /** Calls notification-service placeholder endpoint (no real send yet). */
    public void sendOrderDeliveredPlaceholder(UUID orderId) {
        restClient.post()
                .uri("/internal/notifications")
                .body(Map.of(
                        "orderId", orderId,
                        "message", "Your order " + orderId + " has been delivered."
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
