package com.integrationservice.controller;

import com.integrationservice.dto.PlaceOrderRequestDto;
import com.integrationservice.dto.PlaceOrderResponseDto;
import com.integrationservice.kafka.SagaEventAdapters;
import com.integrationservice.service.SagaOrchestratorService;
import com.integrationservice.web.RequestAuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bench/place-order")
@RequiredArgsConstructor
public class SpringIntegrationBenchController {

    private final SagaOrchestratorService sagaOrchestratorService;

    /**
     * Path C: same Java saga start as /saga, but tags eventAdapter=SPRING_INTEGRATION
     * so OrderSagaIntegrationFlows own the Kafka loop (safe concurrent with /saga).
     */
    @PostMapping("/spring-integration")
    public ResponseEntity<PlaceOrderResponseDto> placeOrderSpringIntegration(
            @RequestBody PlaceOrderRequestDto request) {
        PlaceOrderResponseDto response = sagaOrchestratorService.startPlaceOrder(
                RequestAuthContext.currentUserId(),
                request.getItems(),
                SagaEventAdapters.SPRING_INTEGRATION);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
