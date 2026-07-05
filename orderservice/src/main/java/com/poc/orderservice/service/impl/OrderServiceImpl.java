package com.poc.orderservice.service.impl;

import com.poc.orderservice.dto.CreateOrderRequestDto;
import com.poc.orderservice.dto.OrderResponseDto;
import com.poc.orderservice.dto.OrderSearchRequestDto;
import com.poc.orderservice.dto.SubmitOrderRequestDto;
import com.poc.orderservice.dto.UpdateOrderItemRequestDto;
import com.poc.orderservice.dto.UpdateOrderRequestDto;
import com.poc.orderservice.entity.Order;
import com.poc.orderservice.entity.OrderItem;
import com.poc.orderservice.entity.OrderStatusHistory;
import com.poc.orderservice.event.OrderSubmittedEvent;
import com.poc.orderservice.exception.InvalidOrderRequestException;
import com.poc.orderservice.exception.OrderCreationException;
import com.poc.orderservice.exception.OrderItemNotFoundException;
import com.poc.orderservice.exception.OrderNotFoundException;
import com.poc.orderservice.exception.OrderUpdateNotAllowedException;
import com.poc.orderservice.mapper.OrderMapper;
import com.poc.orderservice.messaging.OrderEventPublisher;
import com.poc.orderservice.repository.OrderRepository;
import com.poc.orderservice.service.OrderService;
import com.poc.orderservice.util.OrderConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequestDto createOrderRequestDto) {
        Order order = orderMapper.toOrder(createOrderRequestDto);
        order.setOrderStatus(OrderConstants.Status.DRAFT);
        long now = Instant.now().toEpochMilli();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setUpdatedBy(order.getCreatedBy());
        List<OrderItem> items = orderMapper.toOrderItems(createOrderRequestDto.items());
        items.forEach(item -> item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))));
        try {
            Order savedOrder = orderRepository.createOrder(order);
            orderRepository.addOrderItems(savedOrder, items);
            savedOrder.setItems(items);

            OrderStatusHistory statusHistory = OrderStatusHistory.builder()
                    .previousStatus(null)
                    .currentStatus(savedOrder.getOrderStatus())
                    .remarks(OrderConstants.Remarks.ORDER_DRAFTED)
                    .changedBy(savedOrder.getCreatedBy())
                    .changedAt(now)
                    .build();
            orderRepository.addOrderStatusHistory(savedOrder, statusHistory);

            return orderMapper.toOrderResponseDto(savedOrder);
        } catch (Exception e) {
            log.error("Failed to create order for customerId={}: {}", createOrderRequestDto.customerId(), e.getMessage(), e);
            throw new OrderCreationException("Failed to create order:" + e.getMessage());
        }
    }

    @Override
    public OrderResponseDto getOrderById(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
        return orderMapper.toOrderResponseDto(order);
    }

    @Override
    public Page<OrderResponseDto> searchOrder(OrderSearchRequestDto orderSearchRequestDto) {
        Page<Order> orders = orderRepository.searchOrders(orderSearchRequestDto);
        return orders.map(orderMapper::toOrderResponseDto);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrder(Long orderId, UpdateOrderRequestDto updateOrderRequestDto) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        assertDraft(order, "updated");

        List<OrderItem> newItems = applyItemUpdates(order, updateOrderRequestDto.items());
        if (!newItems.isEmpty()) {
            orderRepository.addOrderItems(order, newItems);
        }
        recalculateTotalAmount(order);

        long now = Instant.now().toEpochMilli();
        order.setUpdatedBy(updateOrderRequestDto.updatedBy());
        order.setUpdatedAt(now);

        Order updatedOrder = orderRepository.updateOrder(order)
                .orElseThrow(() -> new OrderUpdateNotAllowedException(
                        "Order with id: " + orderId + " cannot be updated as it is not in DRAFT status"));
        updatedOrder.setItems(order.getItems());

        recordStatusHistory(updatedOrder, updateOrderRequestDto, now);

        return orderMapper.toOrderResponseDto(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId, Long deletedBy) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        assertDraft(order, "deleted");

        long now = Instant.now().toEpochMilli();
        Order deletedOrder = orderRepository.deleteOrder(orderId, deletedBy, now)
                .orElseThrow(() -> new OrderUpdateNotAllowedException(
                        "Order with id: " + orderId + " cannot be deleted as it is not in DRAFT status"));

        addStatusHistory(deletedOrder, OrderConstants.Status.DRAFT,
                OrderConstants.Remarks.ORDER_DELETED, deletedBy, now);
    }

    @Override
    @Transactional
    public OrderResponseDto submitOrder(Long orderId, SubmitOrderRequestDto submitOrderRequestDto) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        assertDraft(order, "submitted");

        Long submittedBy = submitOrderRequestDto.submittedBy();

        Order createdOrder = transitionStatus(orderId, OrderConstants.Status.DRAFT, OrderConstants.Status.CREATED,
                submittedBy, order.getItems());
        addStatusHistory(createdOrder, OrderConstants.Status.DRAFT,
                OrderConstants.Remarks.ORDER_CREATED, submittedBy, createdOrder.getUpdatedAt());

        orderEventPublisher.publishOrderSubmitted(buildOrderSubmittedEvent(createdOrder));

        Order submittedOrder = transitionStatus(orderId, OrderConstants.Status.CREATED, OrderConstants.Status.SUBMITTED,
                submittedBy, createdOrder.getItems());
        addStatusHistory(submittedOrder, OrderConstants.Status.CREATED,
                OrderConstants.Remarks.ORDER_SUBMITTED, submittedBy, submittedOrder.getUpdatedAt());

        return orderMapper.toOrderResponseDto(submittedOrder);
    }

    private Order transitionStatus(Long orderId, String expectedStatus, String newStatus, Long changedBy, List<OrderItem> items) {
        Order order = orderRepository.updateOrderStatus(orderId, expectedStatus, newStatus, changedBy, Instant.now().toEpochMilli())
                .orElseThrow(() -> new OrderUpdateNotAllowedException(
                        "Order with id: " + orderId + " cannot transition from " + expectedStatus + " to " + newStatus));
        order.setItems(items);
        return order;
    }

    private OrderSubmittedEvent buildOrderSubmittedEvent(Order order) {
        return new OrderSubmittedEvent(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getCurrency(),
                orderMapper.toOrderItemResponseDtos(order.getItems()),
                order.getUpdatedAt());
    }

    private void assertDraft(Order order, String action) {
        if (!OrderConstants.Status.DRAFT.equals(order.getOrderStatus())) {
            throw new OrderUpdateNotAllowedException(
                    "Order with id: " + order.getId() + " cannot be " + action + " as it is not in DRAFT status");
        }
    }

    private List<OrderItem> applyItemUpdates(Order order, List<UpdateOrderItemRequestDto> itemUpdates) {
        Map<Long, OrderItem> itemsById = order.getItems().stream()
                .collect(Collectors.toMap(OrderItem::getId, Function.identity()));

        List<OrderItem> newItems = new ArrayList<>();
        for (UpdateOrderItemRequestDto itemUpdate : itemUpdates) {
            if (itemUpdate.itemId() != null) {
                updateExistingItem(order.getId(), itemsById, itemUpdate);
            } else {
                OrderItem newItem = buildNewItem(itemUpdate);
                order.addItem(newItem);
                newItems.add(newItem);
            }
        }
        return newItems;
    }

    private void updateExistingItem(Long orderId, Map<Long, OrderItem> itemsById, UpdateOrderItemRequestDto itemUpdate) {
        OrderItem item = itemsById.get(itemUpdate.itemId());
        if (item == null) {
            throw new OrderItemNotFoundException(
                    "Order item not found with id: " + itemUpdate.itemId() + " for order id: " + orderId);
        }
        item.setQuantity(itemUpdate.quantity());
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(itemUpdate.quantity())));
    }

    private OrderItem buildNewItem(UpdateOrderItemRequestDto itemUpdate) {
        if (itemUpdate.productId() == null || itemUpdate.productName() == null
                || itemUpdate.productName().isBlank() || itemUpdate.unitPrice() == null) {
            throw new InvalidOrderRequestException(
                    "productId, productName and unitPrice are required to add a new order item");
        }
        return OrderItem.builder()
                .productId(itemUpdate.productId())
                .productName(itemUpdate.productName())
                .quantity(itemUpdate.quantity())
                .unitPrice(itemUpdate.unitPrice())
                .totalPrice(itemUpdate.unitPrice().multiply(BigDecimal.valueOf(itemUpdate.quantity())))
                .build();
    }

    private void recalculateTotalAmount(Order order) {
        BigDecimal totalAmount = order.getItems().stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
    }

    private void recordStatusHistory(Order order, UpdateOrderRequestDto updateOrderRequestDto, long now) {
        String remarks = updateOrderRequestDto.remarks() != null
                ? updateOrderRequestDto.remarks()
                : OrderConstants.Remarks.ORDER_UPDATED;
        addStatusHistory(order, OrderConstants.Status.DRAFT, remarks, updateOrderRequestDto.updatedBy(), now);
    }

    private void addStatusHistory(Order order, String previousStatus, String remarks, Long changedBy, long changedAt) {
        OrderStatusHistory statusHistory = OrderStatusHistory.builder()
                .previousStatus(previousStatus)
                .currentStatus(order.getOrderStatus())
                .remarks(remarks)
                .changedBy(changedBy)
                .changedAt(changedAt)
                .build();
        orderRepository.addOrderStatusHistory(order, statusHistory);
    }
}
