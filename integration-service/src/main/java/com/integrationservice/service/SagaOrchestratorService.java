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

    PlaceOrderResponseDto startPlaceOrder(UUID customerId, List<PlaceOrderItemDto> items);

    void onOrderCreated(OrderCreatedEvent event);

    void onStockReserved(StockReservedEvent event);

    void onStockReservationFailed(StockReservationFailedEvent event);

    void onOrderConfirmed(OrderConfirmedEvent event);

    void onShipmentUpdated(ShipmentUpdatedEvent event);
}
