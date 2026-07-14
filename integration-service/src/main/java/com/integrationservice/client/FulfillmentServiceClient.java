package com.integrationservice.client;

import com.integrationservice.client.dto.ClientShipmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@RequiredArgsConstructor
public class FulfillmentServiceClient {

    private final RestClient restClient;

    public ClientShipmentResponse fulfillToDelivered(UUID orderId) {
        return restClient.post()
                .uri("/internal/shipments/{orderId}/fulfill-to-delivered", orderId)
                .retrieve()
                .body(ClientShipmentResponse.class);
    }
}
