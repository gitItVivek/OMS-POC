package com.integrationservice.service.impl;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.integrationservice.dto.PlaceOrderItemDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.entity.SagaInstance;
import com.integrationservice.enums.SagaStatus;
import com.integrationservice.enums.SagaStep;
import com.integrationservice.kafka.OmsKafkaTopics;
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

    @Override
    @Transactional
    public PlaceOrderResponseDto startPlaceOrder(UUID customerId, List<PlaceOrderItemDto> items) {
        validateItems(items);

        UUID orderId = UUID.randomUUID();
        SagaInstance saga = SagaInstance.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(customerId)
                .payload(writeItems(items))
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
        log.info("Started place-order saga for orderId={} customerId={}", orderId, customerId);

        return PlaceOrderResponseDto.builder()
                .orderId(orderId)
                .status("IN_PROGRESS")
                .build();
    }

    @Override
    @Transactional
    public void onOrderCreated(OrderCreatedEvent event) {
        SagaInstance saga = findSaga(event.getOrderId());
        saga.setCurrentStep(SagaStep.INVENTORY_RESERVE_SENT);
        sagaInstanceRepository.save(saga);

        ReserveStockCommand command = ReserveStockCommand.builder()
                .orderId(event.getOrderId())
                .items(readItems(saga.getPayload()))
                .build();
        publish(OmsKafkaTopics.INVENTORY_RESERVE_COMMAND, event.getOrderId(), command);
    }

    @Override
    @Transactional
    public void onStockReserved(StockReservedEvent event) {
        SagaInstance saga = findSaga(event.getOrderId());
        saga.setCurrentStep(SagaStep.ORDER_CONFIRM_SENT);
        sagaInstanceRepository.save(saga);

        publish(OmsKafkaTopics.ORDER_CONFIRM_COMMAND, event.getOrderId(),
                OrderConfirmCommand.builder().orderId(event.getOrderId()).build());
    }

    @Override
    @Transactional
    public void onStockReservationFailed(StockReservationFailedEvent event) {
        SagaInstance saga = findSaga(event.getOrderId());
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
        log.warn("Saga failed for orderId={} reason={}", event.getOrderId(), event.getReason());
    }

    @Override
    @Transactional
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        SagaInstance saga = findSaga(event.getOrderId());

        StartFulfillmentCommand command = StartFulfillmentCommand.builder()
                .orderId(event.getOrderId())
                .items(readItems(saga.getPayload()))
                .build();
        publish(OmsKafkaTopics.FULFILLMENT_START_COMMAND, event.getOrderId(), command);
    }

    @Override
    @Transactional
    public void onShipmentUpdated(ShipmentUpdatedEvent event) {
        SagaInstance saga = findSaga(event.getOrderId());
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

        log.info("Saga completed for orderId={} tracking={}", event.getOrderId(), event.getTrackingNumber());
    }

    private void publish(String topic, UUID orderId, Object body) {
        sagaCommandPublisher.publish(topic, orderId, body);
    }

    private SagaInstance findSaga(UUID orderId) {
        return sagaInstanceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Saga not found for order: " + orderId));
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
