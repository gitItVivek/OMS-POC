package com.orderservice.mapper;

import com.orderservice.dto.OrderItemResponseDto;
import com.orderservice.dto.OrderResponseDto;
import com.orderservice.entity.Order;
import com.orderservice.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapper {

    public OrderResponseDto toResponseDto(Order order, List<OrderItem> items) {
        return OrderResponseDto.builder()
                .orderId(order.getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .items(items.stream().map(this::toItemResponseDto).toList())
                .build();
    }

    private OrderItemResponseDto toItemResponseDto(OrderItem item) {
        return OrderItemResponseDto.builder()
                .productId(item.getProductId())
                .productTitleSnapshot(item.getProductTitleSnapshot())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .build();
    }
}
