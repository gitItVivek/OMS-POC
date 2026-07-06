package com.orderservice.client;

import com.orderservice.dto.ProductPageDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.mapper.ProductSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryServiceClient {

    private final RestClient inventoryRestClient;
    private final ProductSummaryMapper productSummaryMapper;

    public ProductPageDto searchProducts(String query, int page, int size) {
        Map<String, Object> response = inventoryRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products/search")
                        .queryParam("q", query)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return productSummaryMapper.fromInventoryPage(response);
    }

    public ProductSummaryDto getProductById(UUID productId) {
        Map<String, Object> response = inventoryRestClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return productSummaryMapper.fromInventoryMap(response);
    }

    public List<ProductSummaryDto> getProductsByIds(List<UUID> productIds) {
        if (productIds.isEmpty()) {
            return List.of();
        }
        List<Map<String, Object>> response = inventoryRestClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/products/by-ids");
                    productIds.forEach(id -> builder.queryParam("ids", id));
                    return builder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        if (response == null) {
            return List.of();
        }
        return response.stream().map(productSummaryMapper::fromInventoryMap).toList();
    }

    public ProductPageDto getProductsByCategory(String category, int page, int size) {
        Map<String, Object> response = inventoryRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/products/by-category")
                        .queryParam("category", category)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return productSummaryMapper.fromInventoryPage(response);
    }
}
