package com.integrationservice.bench;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenchPlaceOrderResponse {

    private UUID orderId;
    private String orchestration;
    private String status;
    private long elapsedMs;
    private String trackingNumber;
    private String message;
}
