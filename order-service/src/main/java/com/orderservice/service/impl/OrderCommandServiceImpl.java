package com.orderservice.service.impl;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dal.OrderDal;
import com.orderservice.dto.OrderCancelCommandDto;
import com.orderservice.dto.OrderCancelledEventDto;
import com.orderservice.dto.OrderConfirmCommandDto;
import com.orderservice.dto.OrderConfirmedEventDto;
import com.orderservice.dto.OrderCreateCommandDto;
import com.orderservice.dto.OrderCreatedEventDto;
import com.orderservice.dto.OrderItemRequestDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.enums.OrderStatus;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.kafka.KafkaPipelineTopics;
import com.orderservice.kafka.OrderEventPublisher;
import com.orderservice.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderDal orderDal;
    private final InventoryServiceClient inventoryServiceClient;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public void handleCreateCommand(OrderCreateCommandDto command) {
        handleCreateCommand(command, KafkaPipelineTopics.SAGA);
    }

    @Override
    @Transactional
    public void handleCreateCommand(OrderCreateCommandDto command, KafkaPipelineTopics topics) {
        if (orderDal.findOrderById(command.getOrderId()).isPresent()) {
            log.warn("Order {} already exists — skipping duplicate create command", command.getOrderId());
            publishCreated(command.getOrderId(), topics);
            return;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = command.getCurrency();

        for (OrderItemRequestDto itemRequest : command.getItems()) {
            ProductSummaryDto product = inventoryServiceClient.getProductById(itemRequest.getProductId());

            if (currency == null) {
                currency = product.getCurrency();
            }

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            orderItems.add(OrderItem.builder()
                    .id(UUID.randomUUID())
                    .productId(product.getProductId())
                    .productTitleSnapshot(product.getTitle())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(product.getPrice())
                    .build());
        }

        if (command.getTotalAmount() != null) {
            totalAmount = command.getTotalAmount();
        }

        Order order = Order.builder()
                .id(command.getOrderId())
                .customerId(command.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .currency(currency != null ? currency : "USD")
                .build();

        Order savedOrder = orderDal.saveOrder(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderDal.saveOrderItems(orderItems);

        publishCreated(savedOrder.getId(), topics);
    }

    @Override
    @Transactional
    public void handleConfirmCommand(OrderConfirmCommandDto command) {
        handleConfirmCommand(command, KafkaPipelineTopics.SAGA);
    }

    @Override
    @Transactional
    public void handleConfirmCommand(OrderConfirmCommandDto command, KafkaPipelineTopics topics) {
        Order order = orderDal.findOrderById(command.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            publishConfirmed(order.getId(), topics);
            return;
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderDal.saveOrder(order);
        publishConfirmed(order.getId(), topics);
    }

    @Override
    @Transactional
    public void handleCancelCommand(OrderCancelCommandDto command) {
        handleCancelCommand(command, KafkaPipelineTopics.SAGA);
    }

    @Override
    @Transactional
    public void handleCancelCommand(OrderCancelCommandDto command, KafkaPipelineTopics topics) {
        Order order = orderDal.findOrderById(command.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            publishCancelled(order.getId(), command.getReason(), topics);
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderDal.saveOrder(order);
        publishCancelled(order.getId(), command.getReason(), topics);
    }

    private void publishCreated(UUID orderId, KafkaPipelineTopics topics) {
        orderEventPublisher.publishOrderCreated(OrderCreatedEventDto.builder()
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .build(), topics.orderCreatedEvent());
    }

    private void publishConfirmed(UUID orderId, KafkaPipelineTopics topics) {
        orderEventPublisher.publishOrderConfirmed(OrderConfirmedEventDto.builder()
                .orderId(orderId)
                .status(OrderStatus.CONFIRMED)
                .build(), topics.orderConfirmedEvent());
    }

    private void publishCancelled(UUID orderId, String reason, KafkaPipelineTopics topics) {
        orderEventPublisher.publishOrderCancelled(OrderCancelledEventDto.builder()
                .orderId(orderId)
                .status(OrderStatus.CANCELLED)
                .reason(reason)
                .build(), topics.orderCancelledEvent());
    }
}
