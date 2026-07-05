package com.orderservice.mapper;

import com.orderservice.dto.ProductPageDto;
import com.orderservice.dto.ProductSummaryDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class ProductSummaryMapper {

    public ProductSummaryDto fromInventoryMap(Map<String, Object> source) {
        return ProductSummaryDto.builder()
                .productId(parseUuid(source.get("productId")))
                .title(stringValue(source.get("title")))
                .brand(stringValue(source.get("brand")))
                .category(stringValue(source.get("category")))
                .price(parseBigDecimal(source.get("price")))
                .currency(stringValue(source.get("currency")))
                .availableQty(parseInteger(source.get("availableQty")))
                .build();
    }

    public ProductPageDto fromInventoryPage(Map<String, Object> source) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) source.getOrDefault("items", List.of());
        return ProductPageDto.builder()
                .items(items.stream().map(this::fromInventoryMap).toList())
                .page(parseInteger(source.get("page")))
                .size(parseInteger(source.get("size")))
                .totalElements(parseLong(source.get("totalElements")))
                .totalPages(parseInteger(source.get("totalPages")))
                .build();
    }

    public List<ProductSummaryDto> deduplicateByProductId(List<ProductSummaryDto> products) {
        Map<UUID, ProductSummaryDto> unique = new LinkedHashMap<>();
        for (ProductSummaryDto product : products) {
            if (product.getProductId() != null) {
                unique.putIfAbsent(product.getProductId(), product);
            }
        }
        return List.copyOf(unique.values());
    }

    private UUID parseUuid(Object value) {
        return value == null ? null : UUID.fromString(value.toString());
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private BigDecimal parseBigDecimal(Object value) {
        return value == null ? null : new BigDecimal(value.toString());
    }

    private Integer parseInteger(Object value) {
        return value == null ? 0 : Integer.parseInt(value.toString());
    }

    private Long parseLong(Object value) {
        return value == null ? 0L : Long.parseLong(value.toString());
    }
}
