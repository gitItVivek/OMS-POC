package com.orderservice.service;

import com.orderservice.dto.OrderCancelCommandDto;
import com.orderservice.dto.OrderConfirmCommandDto;
import com.orderservice.dto.OrderCreateCommandDto;

import java.util.UUID;

/**
 * Bench-only order operations without publishing Kafka events (for Camel HTTP pipeline benchmarks).
 */
public interface OrderBenchService {

    void createOrder(OrderCreateCommandDto command);

    void confirmOrder(OrderConfirmCommandDto command);

    void cancelOrder(OrderCancelCommandDto command);

    String getOrderStatus(UUID orderId);
}
