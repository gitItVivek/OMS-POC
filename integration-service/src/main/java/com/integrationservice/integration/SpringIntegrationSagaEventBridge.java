package com.integrationservice.integration;

import com.integrationservice.kafka.SagaEventAdapters;
import com.integrationservice.messaging.OrderConfirmedEvent;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import com.integrationservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Thin Spring Integration → saga bridge for path C.
 * Forces eventAdapter=SPRING_INTEGRATION so Camel listener ignores these orders.
 */
@Component
@RequiredArgsConstructor
public class SpringIntegrationSagaEventBridge {

    private final SagaOrchestratorService sagaOrchestratorService;

    public void onOrderCreated(OrderCreatedEvent event) {
        sagaOrchestratorService.onOrderCreated(event, SagaEventAdapters.SPRING_INTEGRATION);
    }

    public void onStockReserved(StockReservedEvent event) {
        sagaOrchestratorService.onStockReserved(event, SagaEventAdapters.SPRING_INTEGRATION);
    }

    public void onStockReservationFailed(StockReservationFailedEvent event) {
        sagaOrchestratorService.onStockReservationFailed(event, SagaEventAdapters.SPRING_INTEGRATION);
    }

    public void onOrderConfirmed(OrderConfirmedEvent event) {
        sagaOrchestratorService.onOrderConfirmed(event, SagaEventAdapters.SPRING_INTEGRATION);
    }

    public void onShipmentUpdated(ShipmentUpdatedEvent event) {
        sagaOrchestratorService.onShipmentUpdated(event, SagaEventAdapters.SPRING_INTEGRATION);
    }
}
