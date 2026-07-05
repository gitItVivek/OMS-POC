package com.poc.orderservice.service.impl;

import com.poc.orderservice.dto.CreateOrderRequestDto;
import com.poc.orderservice.dto.OrderResponseDto;
import com.poc.orderservice.dto.OrderSearchRequestDto;
import com.poc.orderservice.entity.Order;
import com.poc.orderservice.entity.OrderItem;
import com.poc.orderservice.entity.OrderStatusHistory;
import com.poc.orderservice.exception.OrderCreationException;
import com.poc.orderservice.exception.OrderNotFoundException;
import com.poc.orderservice.mapper.OrderMapper;
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
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

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
}
