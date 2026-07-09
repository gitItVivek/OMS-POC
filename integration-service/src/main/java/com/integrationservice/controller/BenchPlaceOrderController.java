package com.integrationservice.controller;

import com.integrationservice.bench.BenchOrderStatusResponse;
import com.integrationservice.bench.BenchOrderStatusService;
import com.integrationservice.bench.BenchPlaceOrderRequest;
import com.integrationservice.bench.BenchPlaceOrderResponse;
import com.integrationservice.dto.PlaceOrderRequestDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.kafka.SagaEventAdapters;
import com.integrationservice.service.SagaOrchestratorService;
import com.integrationservice.web.RequestAuthContext;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/bench/place-order")
@RequiredArgsConstructor
public class BenchPlaceOrderController {

    private final SagaOrchestratorService sagaOrchestratorService;
    private final ProducerTemplate producerTemplate;
    private final BenchOrderStatusService benchOrderStatusService;

    /**
     * Path A: tags saga as CAMEL so thin Camel Kafka routes own follow-up events.
     */
    @PostMapping("/saga")
    public ResponseEntity<PlaceOrderResponseDto> placeOrderSaga(@RequestBody PlaceOrderRequestDto request) {
        PlaceOrderResponseDto response = sagaOrchestratorService.startPlaceOrder(
                RequestAuthContext.currentUserId(),
                request.getItems(),
                SagaEventAdapters.CAMEL);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Path B: Camel-heavy pipeline on oms.camel.* (independent of A/C adapters).
     */
    @PostMapping("/camel")
    public ResponseEntity<BenchPlaceOrderResponse> placeOrderCamel(@RequestBody PlaceOrderRequestDto request) {
        BenchPlaceOrderRequest pipelineRequest = BenchPlaceOrderRequest.builder()
                .customerId(RequestAuthContext.currentUserId())
                .items(request.getItems())
                .build();
        BenchPlaceOrderResponse response = producerTemplate.requestBody(
                "direct:bench-place-order",
                pipelineRequest,
                BenchPlaceOrderResponse.class);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Shared status for A/B/C.
     */
    @GetMapping("/{orderId}/status")
    public BenchOrderStatusResponse status(@PathVariable UUID orderId) {
        return benchOrderStatusService.getStatus(orderId);
    }
}
