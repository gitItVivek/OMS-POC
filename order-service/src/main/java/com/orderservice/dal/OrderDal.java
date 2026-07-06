package com.orderservice.dal;

import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderDal {

    Order saveOrder(Order order);

    void saveOrderItems(List<OrderItem> items);

    Optional<Order> findOrderById(UUID orderId);

    List<Order> findOrdersByCustomerId(UUID customerId);

    List<OrderItem> findItemsByOrderId(UUID orderId);
}
