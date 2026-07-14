package com.orderservice.service.impl;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dal.OrderDal;
import com.orderservice.dto.InternalCreateOrderRequestDto;
import com.orderservice.dto.OrderItemRequestDto;
import com.orderservice.dto.OrderResponseDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.enums.OrderStatus;
import com.orderservice.exception.InsufficientStockException;
import com.orderservice.exception.OrderNotFoundException;
import com.orderservice.mapper.OrderMapper;
import com.orderservice.service.OrderLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderLifecycleServiceImpl implements OrderLifecycleService {

    private final OrderDal orderDal;
    private final OrderMapper orderMapper;
    private final InventoryServiceClient inventoryServiceClient;

    @Override
    @Transactional
    public OrderResponseDto createOrderForCustomer(InternalCreateOrderRequestDto request) {
        validateCreateRequest(request);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        String currency = null;

        for (OrderItemRequestDto itemRequest : request.getItems()) {
            ProductSummaryDto product = inventoryServiceClient.getProductById(itemRequest.getProductId());
            if (product.getAvailableQty() == null || product.getAvailableQty() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        itemRequest.getProductId(),
                        itemRequest.getQuantity(),
                        product.getAvailableQty() == null ? 0 : product.getAvailableQty());
            }
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

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .currency(currency)
                .build();

        Order savedOrder = orderDal.saveOrder(order);
        orderItems.forEach(item -> item.setOrder(savedOrder));
        orderDal.saveOrderItems(orderItems);
        return orderMapper.toResponseDto(savedOrder, orderItems);
    }

    @Override
    @Transactional
    public OrderResponseDto confirmOrder(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return orderMapper.toResponseDto(order, orderDal.findItemsByOrderId(orderId));
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot confirm order in status " + order.getStatus());
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return orderMapper.toResponseDto(orderDal.saveOrder(order), orderDal.findItemsByOrderId(orderId));
    }

    @Override
    @Transactional
    public OrderResponseDto cancelOrder(UUID orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return orderMapper.toResponseDto(order, orderDal.findItemsByOrderId(orderId));
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderMapper.toResponseDto(orderDal.saveOrder(order), orderDal.findItemsByOrderId(orderId));
    }

    private Order findOrder(UUID orderId) {
        return orderDal.findOrderById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private void validateCreateRequest(InternalCreateOrderRequestDto request) {
        if (request.getCustomerId() == null) {
            throw new IllegalArgumentException("customerId is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        for (OrderItemRequestDto item : request.getItems()) {
            if (item.getProductId() == null) {
                throw new IllegalArgumentException("Each order item must have a productId");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new IllegalArgumentException("Each order item must have quantity >= 1");
            }
        }
    }
}
