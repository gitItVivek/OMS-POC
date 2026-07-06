package com.orderservice.service.impl;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dal.OrderDal;
import com.orderservice.dto.OrderCancelCommandDto;
import com.orderservice.dto.OrderConfirmCommandDto;
import com.orderservice.dto.OrderCreateCommandDto;
import com.orderservice.dto.OrderItemRequestDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.enums.OrderStatus;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.service.OrderBenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderBenchServiceImpl implements OrderBenchService {

    private final OrderDal orderDal;
    private final InventoryServiceClient inventoryServiceClient;

    @Override
    @Transactional
    public void createOrder(OrderCreateCommandDto command) {
        if (orderDal.findOrderById(command.getOrderId()).isPresent()) {
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
    }

    @Override
    @Transactional
    public void confirmOrder(OrderConfirmCommandDto command) {
        Order order = orderDal.findOrderById(command.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderDal.saveOrder(order);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(OrderCancelCommandDto command) {
        Order order = orderDal.findOrderById(command.getOrderId())
                .orElseThrow(() -> new OrderNotFoundException(command.getOrderId()));
        if (order.getStatus() != OrderStatus.CANCELLED) {
            order.setStatus(OrderStatus.CANCELLED);
            orderDal.saveOrder(order);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getOrderStatus(UUID orderId) {
        return orderDal.findOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId))
                .getStatus()
                .name();
    }
}
