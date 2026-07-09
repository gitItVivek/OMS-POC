package com.integrationservice.service;

import com.integrationservice.dto.PlaceOrderItemDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.messaging.OrderConfirmedEvent;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;

import java.util.List;
import java.util.UUID;

public interface SagaOrchestratorService {

    /**
     * @param eventAdapter which Kafka listener owns follow-up events for this order
     *                     ({@code CAMEL} or {@code SPRING_INTEGRATION})
     */
    PlaceOrderResponseDto startPlaceOrder(UUID customerId, List<PlaceOrderItemDto> items, String eventAdapter);

    void onOrderCreated(OrderCreatedEvent event, String eventAdapter);

    void onStockReserved(StockReservedEvent event, String eventAdapter);

    void onStockReservationFailed(StockReservationFailedEvent event, String eventAdapter);

    void onOrderConfirmed(OrderConfirmedEvent event, String eventAdapter);

    void onShipmentUpdated(ShipmentUpdatedEvent event, String eventAdapter);
}
