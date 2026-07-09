package com.integrationservice.service.impl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.integrationservice.dto.PlaceOrderItemDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.entity.SagaInstance;
import com.integrationservice.enums.SagaStatus;
import com.integrationservice.enums.SagaStep;
import com.integrationservice.kafka.OmsKafkaTopics;
import com.integrationservice.kafka.SagaEventAdapters;
import com.integrationservice.messaging.OrderCancelCommand;
import com.integrationservice.messaging.OrderConfirmCommand;
import com.integrationservice.messaging.OrderConfirmedEvent;
import com.integrationservice.messaging.OrderCreateCommand;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.OrderLineItem;
import com.integrationservice.messaging.ReleaseStockCommand;
import com.integrationservice.messaging.ReserveStockCommand;
import com.integrationservice.messaging.SendNotificationCommand;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StartFulfillmentCommand;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import com.integrationservice.repository.SagaInstanceRepository;
import com.integrationservice.kafka.SagaCommandPublisher;
import com.integrationservice.service.SagaOrchestratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SagaOrchestratorServiceImpl implements SagaOrchestratorService {

    private final SagaInstanceRepository sagaInstanceRepository;
    private final SagaCommandPublisher sagaCommandPublisher;
    private final ObjectMapper objectMapper;

    /**
     * Shared start for /saga and /spring-integration (and demo /place-order).
     * Persists eventAdapter so only the matching Kafka listener advances this order.
     */
    @Override
    @Transactional
    public PlaceOrderResponseDto startPlaceOrder(UUID customerId, List<PlaceOrderItemDto> items, String eventAdapter) {
        validateItems(items);
        String adapter = normalizeAdapter(eventAdapter);

        UUID orderId = UUID.randomUUID();
        SagaInstance saga = SagaInstance.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .payload(writeItems(items))
                .eventAdapter(adapter)
                .currentStep(SagaStep.ORDER_CREATE_SENT)
                .status(SagaStatus.IN_PROGRESS)
                .build();
        sagaInstanceRepository.save(saga);

        OrderCreateCommand command = OrderCreateCommand.builder()
                .orderId(orderId)
                .customerId(customerId)
                .items(toLineItems(items))
                .build();

        publish(OmsKafkaTopics.ORDER_CREATE_COMMAND, orderId, command);
        log.info("Started place-order saga for orderId={} customerId={} eventAdapter={}",
                orderId, customerId, adapter);

        return PlaceOrderResponseDto.builder()
                .orderId(orderId)
                .status("IN_PROGRESS")
                .build();
    }

    /**
     * Step 1: order created → reserve stock. Caller must be the adapter stored on the saga.
     */
    @Override
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event, String eventAdapter) {
        SagaInstance saga = findOwnedSaga(event.getOrderId(), eventAdapter);
        if (saga == null) {
            return;
        }
        saga.setCurrentStep(SagaStep.INVENTORY_RESERVE_SENT);
        sagaInstanceRepository.save(saga);

        ReserveStockCommand command = ReserveStockCommand.builder()
                .orderId(event.getOrderId())
                .items(readItems(saga.getPayload()))
                .build();
        publish(OmsKafkaTopics.INVENTORY_RESERVE_COMMAND, event.getOrderId(), command);
    }

    /**
     * Step 2: stock reserved → confirm order.
     */
    @Override
    @Transactional
    public void onStockReserved(StockReservedEvent event, String eventAdapter) {
        SagaInstance saga = findOwnedSaga(event.getOrderId(), eventAdapter);
        if (saga == null) {
            return;
        }
        saga.setCurrentStep(SagaStep.ORDER_CONFIRM_SENT);
        sagaInstanceRepository.save(saga);

        publish(OmsKafkaTopics.ORDER_CONFIRM_COMMAND, event.getOrderId(),
                OrderConfirmCommand.builder().orderId(event.getOrderId()).build());
    }

    /**
     * Compensation: reservation failed → cancel order.
     */
    @Override
    @Transactional
    public void onStockReservationFailed(StockReservationFailedEvent event, String eventAdapter) {
        SagaInstance saga = findOwnedSaga(event.getOrderId(), eventAdapter);
        if (saga == null) {
            return;
        }
        saga.setCurrentStep(SagaStep.COMPENSATING);
        saga.setStatus(SagaStatus.FAILED);
        sagaInstanceRepository.save(saga);

        publish(OmsKafkaTopics.ORDER_CANCEL_COMMAND, event.getOrderId(),
                OrderCancelCommand.builder()
                        .orderId(event.getOrderId())
                        .reason(event.getReason())
                        .build());

        saga.setCurrentStep(SagaStep.CANCELLED);
        sagaInstanceRepository.save(saga);
        log.warn("Saga failed for orderId={} reason={} eventAdapter={}",
                event.getOrderId(), event.getReason(), eventAdapter);
    }

    /**
     * Step 3: order confirmed → start fulfillment.
     */
    @Override
    @Transactional
    public void onOrderConfirmed(OrderConfirmedEvent event, String eventAdapter) {
        SagaInstance saga = findOwnedSaga(event.getOrderId(), eventAdapter);
        if (saga == null) {
            return;
        }

        StartFulfillmentCommand command = StartFulfillmentCommand.builder()
                .orderId(event.getOrderId())
                .items(readItems(saga.getPayload()))
                .build();
        publish(OmsKafkaTopics.FULFILLMENT_START_COMMAND, event.getOrderId(), command);
    }

    /**
     * Final step: shipment updated → COMPLETED + notification command.
     */
    @Override
    @Transactional
    public void onShipmentUpdated(ShipmentUpdatedEvent event, String eventAdapter) {
        SagaInstance saga = findOwnedSaga(event.getOrderId(), eventAdapter);
        if (saga == null) {
            return;
        }
        saga.setCurrentStep(SagaStep.COMPLETED);
        saga.setStatus(SagaStatus.COMPLETED);
        sagaInstanceRepository.save(saga);

        publish(OmsKafkaTopics.NOTIFICATION_SEND_COMMAND, event.getOrderId(),
                SendNotificationCommand.builder()
                        .orderId(event.getOrderId())
                        .notificationType("ORDER_SHIPPED")
                        .message("Your order " + event.getOrderId() + " has shipped. Tracking: "
                                + event.getTrackingNumber())
                        .build());

        log.info("Saga completed for orderId={} tracking={} eventAdapter={}",
                event.getOrderId(), event.getTrackingNumber(), eventAdapter);
    }

    private void publish(String topic, UUID orderId, Object body) {
        sagaCommandPublisher.publish(topic, orderId, body);
    }

    /**
     * Returns the saga only if this listener owns it; otherwise ignores the event
     * (the other adapter — Camel or SI — will process that order).
     */
    private SagaInstance findOwnedSaga(UUID orderId, String eventAdapter) {
        SagaInstance saga = sagaInstanceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Saga not found for order: " + orderId));
        String expected = normalizeAdapter(eventAdapter);
        String actual = saga.getEventAdapter() != null ? saga.getEventAdapter() : SagaEventAdapters.CAMEL;
        if (!expected.equals(actual)) {
            log.debug("Ignoring event for orderId={} — owned by {} but listener is {}",
                    orderId, actual, expected);
            return null;
        }
        return saga;
    }

    private String normalizeAdapter(String eventAdapter) {
        if (eventAdapter == null || eventAdapter.isBlank()) {
            return SagaEventAdapters.CAMEL;
        }
        String value = eventAdapter.trim().toUpperCase().replace('-', '_');
        if (SagaEventAdapters.SPRING_INTEGRATION.equals(value)
                || "SPRING_INTEGRATION".equals(value)
                || "SI".equals(value)) {
            return SagaEventAdapters.SPRING_INTEGRATION;
        }
        if (SagaEventAdapters.CAMEL.equals(value)) {
            return SagaEventAdapters.CAMEL;
        }
        throw new IllegalArgumentException("Unknown saga event adapter: " + eventAdapter
                + " (expected CAMEL or SPRING_INTEGRATION)");
    }

    private void validateItems(List<PlaceOrderItemDto> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (PlaceOrderItemDto item : items) {
            if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() < 1) {
                throw new IllegalArgumentException("Each item must have productId and quantity >= 1");
            }
        }
    }

    private List<OrderLineItem> toLineItems(List<PlaceOrderItemDto> items) {
        return items.stream()
                .map(item -> OrderLineItem.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .toList();
    }

    private String writeItems(List<PlaceOrderItemDto> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize saga items", e);
        }
    }

    private List<OrderLineItem> readItems(String payload) {
        try {
            List<PlaceOrderItemDto> items = objectMapper.readValue(payload, new TypeReference<>() {
            });
            return toLineItems(items);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize saga items", e);
        }
    }
}
