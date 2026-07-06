package com.orderservice.dal.impl;

import com.orderservice.dal.OrderDal;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import com.orderservice.repository.OrderItemRepository;
import com.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderDalImpl implements OrderDal {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void saveOrderItems(List<OrderItem> items) {
        orderItemRepository.saveAll(items);
    }

    @Override
    public Optional<Order> findOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> findOrdersByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Override
    public List<OrderItem> findItemsByOrderId(UUID orderId) {
        return orderItemRepository.findByOrder_Id(orderId);
    }
}
