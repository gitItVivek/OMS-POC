package com.integrationservice.client;

import com.integrationservice.client.dto.ClientReleaseStockRequest;
import com.integrationservice.client.dto.ClientReserveStockRequest;
import com.integrationservice.client.dto.ClientReserveStockItem;
import com.integrationservice.client.dto.ClientStockReservationResult;
import com.integrationservice.client.dto.ClientOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@RequiredArgsConstructor
public class InventoryServiceClient {

    private final RestClient restClient;

    public ClientStockReservationResult reserveStock(UUID orderId, ClientOrderResponse order) {
        return restClient.post()
                .uri("/internal/stock/reserve")
                .body(ClientReserveStockRequest.builder()
                        .orderId(orderId)
                        .items(order.getItems().stream()
                                .map(item -> ClientReserveStockItem.builder()
                                        .productId(item.getProductId())
                                        .quantity(item.getQuantity())
                                        .build())
                                .toList())
                        .build())
                .retrieve()
                .body(ClientStockReservationResult.class);
    }

    public void releaseStock(UUID orderId) {
        restClient.post()
                .uri("/internal/stock/release")
                .body(ClientReleaseStockRequest.builder().orderId(orderId).build())
                .retrieve()
                .toBodilessEntity();
    }
}
