package com.integrationservice.camel;

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
 * Thin Camel → saga bridge for path A.
 * Forces eventAdapter=CAMEL so SI listener ignores these orders.
 */
@Component
@RequiredArgsConstructor
public class CamelSagaEventBridge {

    private final SagaOrchestratorService sagaOrchestratorService;

    public void onOrderCreated(OrderCreatedEvent event) {
        sagaOrchestratorService.onOrderCreated(event, SagaEventAdapters.CAMEL);
    }

    public void onStockReserved(StockReservedEvent event) {
        sagaOrchestratorService.onStockReserved(event, SagaEventAdapters.CAMEL);
    }

    public void onStockReservationFailed(StockReservationFailedEvent event) {
        sagaOrchestratorService.onStockReservationFailed(event, SagaEventAdapters.CAMEL);
    }

    public void onOrderConfirmed(OrderConfirmedEvent event) {
        sagaOrchestratorService.onOrderConfirmed(event, SagaEventAdapters.CAMEL);
    }

    public void onShipmentUpdated(ShipmentUpdatedEvent event) {
        sagaOrchestratorService.onShipmentUpdated(event, SagaEventAdapters.CAMEL);
    }
}
