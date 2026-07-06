package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateCommandDto {

    private UUID orderId;
    private UUID customerId;
    private List<OrderItemRequestDto> items;
    private BigDecimal totalAmount;
    private String currency;
}
