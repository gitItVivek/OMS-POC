package com.orderservice.config;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dto.OrderItemRequestDto;
import com.orderservice.dto.OrderItemStockResult;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.integration.StockLookupGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.IntegrationComponentScan;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * Exercises the real Spring Integration flow assembled in {@link StockLookupIntegrationConfig}
 * together with {@link StockLookupGateway}: split -> executor channel -> handle -> aggregate.
 * {@link InventoryServiceClient} is mocked so only the parallel wiring/aggregation/error-routing
 * behavior of the flow itself is under test, matching the documented behavior in
 * order-service/SPRING_INTEGRATION.md.
 */
@SpringJUnitConfig
class StockLookupFlowIntegrationTest {

    @Configuration
    @EnableIntegration
    @IntegrationComponentScan(basePackageClasses = StockLookupGateway.class)
    @Import(StockLookupIntegrationConfig.class)
    static class TestContext {

        @Bean
        InventoryServiceClient inventoryServiceClient() {
            return mock(InventoryServiceClient.class);
        }
    }

    @Autowired
    private StockLookupGateway stockLookupGateway;

    @Autowired
    private InventoryServiceClient inventoryServiceClient;

    @BeforeEach
    void resetMock() {
        reset(inventoryServiceClient);
    }

    @Test
    void lookupStock_resolvesProductForEveryRequestedItem() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        UUID productId3 = UUID.randomUUID();

        OrderItemRequestDto item1 = OrderItemRequestDto.builder().productId(productId1).quantity(2).build();
        OrderItemRequestDto item2 = OrderItemRequestDto.builder().productId(productId2).quantity(1).build();
        OrderItemRequestDto item3 = OrderItemRequestDto.builder().productId(productId3).quantity(4).build();

        when(inventoryServiceClient.getProductById(productId1)).thenReturn(
                ProductSummaryDto.builder().productId(productId1).availableQty(10).price(BigDecimal.TEN).currency("USD").build());
        when(inventoryServiceClient.getProductById(productId2)).thenReturn(
                ProductSummaryDto.builder().productId(productId2).availableQty(5).price(BigDecimal.ONE).currency("USD").build());
        when(inventoryServiceClient.getProductById(productId3)).thenReturn(
                ProductSummaryDto.builder().productId(productId3).availableQty(0).price(BigDecimal.valueOf(2)).currency("USD").build());

        List<OrderItemStockResult> results = stockLookupGateway.lookupStock(List.of(item1, item2, item3));

        assertThat(results).hasSize(3);
        assertThat(results)
                .extracting(result -> result.getItemRequest().getProductId())
                .containsExactlyInAnyOrder(productId1, productId2, productId3);
        assertThat(results)
                .extracting(result -> result.getProduct().getAvailableQty())
                .containsExactlyInAnyOrder(10, 5, 0);
    }

    @Test
    void lookupStock_withSingleItem_returnsSingleResolvedResult() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto item = OrderItemRequestDto.builder().productId(productId).quantity(1).build();
        ProductSummaryDto product = ProductSummaryDto.builder()
                .productId(productId).availableQty(3).price(BigDecimal.ONE).currency("EUR").build();

        when(inventoryServiceClient.getProductById(productId)).thenReturn(product);

        List<OrderItemStockResult> results = stockLookupGateway.lookupStock(List.of(item));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getItemRequest()).isSameAs(item);
        assertThat(results.get(0).getProduct()).isSameAs(product);
    }

    @Test
    void lookupStock_whenInventoryClientThrowsRuntimeException_propagatesSameException() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto item = OrderItemRequestDto.builder().productId(productId).quantity(1).build();

        when(inventoryServiceClient.getProductById(productId))
                .thenThrow(new IllegalStateException("inventory-service unavailable"));

        assertThatThrownBy(() -> stockLookupGateway.lookupStock(List.of(item)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("inventory-service unavailable");
    }

    @Test
    void lookupStock_whenCauseIsNotARuntimeException_wrapsInIllegalStateException() {
        UUID productId = UUID.randomUUID();
        OrderItemRequestDto item = OrderItemRequestDto.builder().productId(productId).quantity(1).build();

        IOException checkedCause = new IOException("network down");
        when(inventoryServiceClient.getProductById(productId)).thenThrow(checkedCause);

        assertThatThrownBy(() -> stockLookupGateway.lookupStock(List.of(item)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Stock lookup failed");
    }
}