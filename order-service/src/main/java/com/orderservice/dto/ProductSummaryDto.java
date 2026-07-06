package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryDto {

    private UUID productId;
    private String title;
    private String brand;
    private String category;
    private BigDecimal price;
    private String currency;
    private Integer availableQty;
}
