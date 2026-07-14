package com.integrationservice.service.impl;

import com.integrationservice.client.FulfillmentServiceClient;
import com.integrationservice.client.InventoryServiceClient;
import com.integrationservice.client.NotificationServiceClient;
import com.integrationservice.client.OrderServiceClient;
import com.integrationservice.client.dto.ClientOrderResponse;
import com.integrationservice.client.dto.ClientStockReservationResult;
import com.integrationservice.dto.PlaceOrderRequestDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.service.SyncPlaceOrderOrchestrator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncPlaceOrderOrchestratorImpl implements SyncPlaceOrderOrchestrator {

    private final OrderServiceClient orderServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final FulfillmentServiceClient fulfillmentServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    public PlaceOrderResponseDto placeOrder(PlaceOrderRequestDto request) {
        validateRequest(request);
        List<String> stepsCompleted = new ArrayList<>();

        ClientOrderResponse order = orderServiceClient.createOrder(request);
        UUID orderId = order.getOrderId();
        stepsCompleted.add("ORDER_CREATED");

        ClientStockReservationResult reservation = inventoryServiceClient.reserveStock(orderId, order);
        if (!reservation.isSuccess()) {
            orderServiceClient.cancelOrder(orderId);
            stepsCompleted.add("ORDER_CANCELLED");
            return PlaceOrderResponseDto.builder()
                    .orderId(orderId)
                    .status("CANCELLED")
                    .orchestrationMode("SYNC_HTTP")
                    .stepsCompleted(stepsCompleted)
                    .message(reservation.getMessage())
                    .build();
        }
        stepsCompleted.add("STOCK_RESERVED");

        ClientOrderResponse confirmed = orderServiceClient.confirmOrder(orderId);
        stepsCompleted.add("ORDER_CONFIRMED");

        fulfillmentServiceClient.fulfillToDelivered(orderId);
        stepsCompleted.add("SHIPMENT_DELIVERED");

        try {
            notificationServiceClient.sendOrderDeliveredPlaceholder(orderId);
            stepsCompleted.add("NOTIFICATION_PLACEHOLDER");
        } catch (Exception ex) {
            log.warn("Notification placeholder call failed for order {}: {}", orderId, ex.getMessage());
            stepsCompleted.add("NOTIFICATION_PLACEHOLDER_SKIPPED");
        }

        return PlaceOrderResponseDto.builder()
                .orderId(orderId)
                .status(confirmed.getStatus())
                .orchestrationMode("SYNC_HTTP")
                .stepsCompleted(stepsCompleted)
                .message("Order completed synchronously over HTTP")
                .build();
    }

    private void validateRequest(PlaceOrderRequestDto request) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("At least one item is required");
        }
    }
}
