package com.poc.orderservice.mapper;

import com.poc.orderservice.dto.CreateOrderRequestDto;
import com.poc.orderservice.dto.OrderItemDto;
import com.poc.orderservice.dto.OrderItemResponseDto;
import com.poc.orderservice.dto.OrderResponseDto;
import com.poc.orderservice.entity.Order;
import com.poc.orderservice.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedBy", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "statusHistory", ignore = true)
    Order toOrder(CreateOrderRequestDto createOrderRequestDto);

    List<OrderItem> toOrderItems(List<OrderItemDto> orderItemDtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    OrderItem toOrderItem(OrderItemDto orderItemDto);

    OrderResponseDto toOrderResponseDto(Order order);

    List<OrderItemResponseDto> toOrderItemResponseDtos(List<OrderItem> orderItems);

    OrderItemResponseDto toOrderItemResponseDto(OrderItem orderItem);
}
