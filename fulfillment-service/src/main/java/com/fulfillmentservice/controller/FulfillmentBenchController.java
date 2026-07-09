package com.fulfillmentservice.controller;

import com.fulfillmentservice.dto.BenchFulfillmentResponseDto;
import com.fulfillmentservice.dto.StartFulfillmentCommandDto;
import com.fulfillmentservice.service.FulfillmentBenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/bench")
@RequiredArgsConstructor
public class FulfillmentBenchController {

    private final FulfillmentBenchService fulfillmentBenchService;

    @PostMapping("/shipments")
    public BenchFulfillmentResponseDto startShipment(@RequestBody StartFulfillmentCommandDto command) {
        String trackingNumber = fulfillmentBenchService.startFulfillment(command);
        return BenchFulfillmentResponseDto.builder()
                .success(true)
                .step("START_FULFILLMENT")
                .trackingNumber(trackingNumber)
                .build();
    }
}
