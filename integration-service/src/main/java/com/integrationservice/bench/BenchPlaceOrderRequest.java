package com.integrationservice.bench;

import com.integrationservice.dto.PlaceOrderItemDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenchPlaceOrderRequest {

    private UUID customerId;
    private List<PlaceOrderItemDto> items;
}
