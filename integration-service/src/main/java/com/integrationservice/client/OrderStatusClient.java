package com.integrationservice.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderStatusClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${order.service.base-url}")
    private String orderServiceBaseUrl;

    public String getOrderStatus(UUID orderId) {
        try {
            Map<?, ?> response = restClientBuilder.baseUrl(orderServiceBaseUrl).build()
                    .get()
                    .uri("/internal/bench/orders/{orderId}/status", orderId)
                    .retrieve()
                    .body(Map.class);
            if (response == null || response.get("status") == null) {
                return "UNKNOWN";
            }
            return response.get("status").toString();
        } catch (Exception e) {
            return "NOT_FOUND";
        }
    }
}
