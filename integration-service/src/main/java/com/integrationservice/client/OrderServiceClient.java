package com.integrationservice.client;

import com.integrationservice.client.dto.ClientCreateOrderRequest;
import com.integrationservice.client.dto.ClientOrderItemRequest;
import com.integrationservice.client.dto.ClientOrderResponse;
import com.integrationservice.dto.PlaceOrderItemDto;
import com.integrationservice.dto.PlaceOrderRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class OrderServiceClient {

    private final RestClient restClient;

    public ClientOrderResponse createOrder(PlaceOrderRequestDto request) {
        return restClient.post()
                .uri("/internal/orders")
                .body(ClientCreateOrderRequest.builder()
                        .customerId(request.getCustomerId())
                        .items(mapItems(request.getItems()))
                        .build())
                .retrieve()
                .body(ClientOrderResponse.class);
    }

    public ClientOrderResponse confirmOrder(UUID orderId) {
        return restClient.post()
                .uri("/internal/orders/{orderId}/confirm", orderId)
                .retrieve()
                .body(ClientOrderResponse.class);
    }

    public ClientOrderResponse cancelOrder(UUID orderId) {
        return restClient.post()
                .uri("/internal/orders/{orderId}/cancel", orderId)
                .retrieve()
                .body(ClientOrderResponse.class);
    }

    private List<ClientOrderItemRequest> mapItems(List<PlaceOrderItemDto> items) {
        return items.stream()
                .map(item -> ClientOrderItemRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();
    }
}
