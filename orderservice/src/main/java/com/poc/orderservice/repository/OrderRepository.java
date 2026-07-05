package com.poc.orderservice.repository;

import com.poc.orderservice.entity.Order;
import com.poc.orderservice.entity.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order createOrder(Order order);

    List<OrderItem> addOrderItems(Order order, List<OrderItem> items);

    Optional<Order> findByIdWithItems(Long orderId);
}
