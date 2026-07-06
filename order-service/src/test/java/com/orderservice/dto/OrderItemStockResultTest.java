package com.orderservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemStockResultTest {

    @Test
    void builder_setsAllFields() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder()
                .productId(productId)
                .quantity(3)
                .build();
        ProductSummaryDto product = ProductSummaryDto.builder()
                .productId(productId)
                .title("Widget")
                .price(BigDecimal.valueOf(9.99))
                .currency("USD")
                .availableQty(10)
                .build();

        OrderItemStockResult result = OrderItemStockResult.builder()
                .itemRequest(itemRequest)
                .product(product)
                .build();

        assertThat(result.getItemRequest()).isSameAs(itemRequest);
        assertThat(result.getProduct()).isSameAs(product);
    }

    @Test
    void noArgsConstructor_startsEmptyAndSettersPopulateFields() {
        OrderItemStockResult result = new OrderItemStockResult();
        assertThat(result.getItemRequest()).isNull();
        assertThat(result.getProduct()).isNull();

        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();
        ProductSummaryDto product = ProductSummaryDto.builder().availableQty(5).build();

        result.setItemRequest(itemRequest);
        result.setProduct(product);

        assertThat(result.getItemRequest()).isSameAs(itemRequest);
        assertThat(result.getProduct()).isSameAs(product);
    }

    @Test
    void allArgsConstructor_setsFieldsInDeclaredOrder() {
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder()
                .productId(UUID.randomUUID())
                .quantity(2)
                .build();
        ProductSummaryDto product = ProductSummaryDto.builder().availableQty(7).build();

        OrderItemStockResult result = new OrderItemStockResult(itemRequest, product);

        assertThat(result.getItemRequest()).isSameAs(itemRequest);
        assertThat(result.getProduct()).isSameAs(product);
    }

    @Test
    void builder_canBuildWithOnlyOneFieldSet() {
        OrderItemRequestDto itemRequest = OrderItemRequestDto.builder()
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        OrderItemStockResult result = OrderItemStockResult.builder()
                .itemRequest(itemRequest)
                .build();

        assertThat(result.getItemRequest()).isSameAs(itemRequest);
        assertThat(result.getProduct()).isNull();
    }
}