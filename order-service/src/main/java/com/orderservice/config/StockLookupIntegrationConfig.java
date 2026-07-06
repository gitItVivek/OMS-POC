package com.orderservice.config;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dto.OrderItemRequestDto;
import com.orderservice.dto.OrderItemStockResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessagingException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class StockLookupIntegrationConfig {

    @Bean
    public Executor stockLookupExecutor() {
        return Executors.newFixedThreadPool(8);
    }

    @Bean
    public IntegrationFlow stockLookupFlow(InventoryServiceClient inventoryServiceClient, Executor stockLookupExecutor) {
        return IntegrationFlow.from("stockLookupRequestChannel")
                .split()
                .channel(c -> c.executor(stockLookupExecutor))
                .handle(OrderItemRequestDto.class, (itemRequest, headers) -> OrderItemStockResult.builder()
                        .itemRequest(itemRequest)
                        .product(inventoryServiceClient.getProductById(itemRequest.getProductId()))
                        .build())
                .aggregate()
                .channel("stockLookupReplyChannel")
                .get();
    }

    @Bean
    public IntegrationFlow stockLookupErrorFlow() {
        return IntegrationFlow.from("stockLookupErrorChannel")
                .<MessagingException>handle((exception, headers) -> {
                    Throwable cause = exception.getCause() != null ? exception.getCause() : exception;
                    if (cause instanceof RuntimeException runtimeException) {
                        throw runtimeException;
                    }
                    throw new IllegalStateException("Stock lookup failed", cause);
                })
                .get();
    }
}
