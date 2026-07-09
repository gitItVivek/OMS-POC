package com.integrationservice.bench;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.integrationservice.dto.PlaceOrderItemDto;
import com.integrationservice.entity.PipelineRun;
import com.integrationservice.messaging.OrderCancelCommand;
import com.integrationservice.messaging.OrderConfirmCommand;
import com.integrationservice.messaging.OrderCreateCommand;
import com.integrationservice.messaging.OrderCreatedEvent;
import com.integrationservice.messaging.OrderLineItem;
import com.integrationservice.messaging.ReleaseStockCommand;
import com.integrationservice.messaging.ReserveStockCommand;
import com.integrationservice.messaging.ShipmentUpdatedEvent;
import com.integrationservice.messaging.StartFulfillmentCommand;
import com.integrationservice.messaging.StockReservationFailedEvent;
import com.integrationservice.messaging.StockReservedEvent;
import com.integrationservice.repository.PipelineRunRepository;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlaceOrderPipelineProcessor {

    public static final String PROP_PIPELINE_RUN = "pipelineRun";

    private final PipelineRunRepository pipelineRunRepository;
    private final ObjectMapper objectMapper;

    /**
     * Initializes B-path runtime state and persists pipeline_runs row before sending any command.
     */
    public void initialize(Exchange exchange) {
        BenchPlaceOrderRequest request = exchange.getMessage().getBody(BenchPlaceOrderRequest.class);
        UUID orderId = UUID.randomUUID();

        PipelineRun run = PipelineRun.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .customerId(request.getCustomerId())
                .orchestration("CAMEL_PIPELINE")
                .status("IN_PROGRESS")
                .payload(writeItems(request.getItems()))
                .build();
        pipelineRunRepository.save(run);
        exchange.setProperty(PROP_PIPELINE_RUN, run);
    }

    /**
     * Builds first command in B-path (create order).
     */
    public OrderCreateCommand buildCreateOrderCommand(Exchange exchange) {
        PipelineRun run = exchange.getProperty(PROP_PIPELINE_RUN, PipelineRun.class);
        return OrderCreateCommand.builder()
                .orderId(run.getOrderId())
                .customerId(run.getCustomerId())
                .items(readItems(run.getPayload()))
                .build();
    }

    /**
     * Returns immediate HTTP response while Camel orchestration continues asynchronously on Kafka.
     */
    public BenchPlaceOrderResponse buildAcceptedResponse(Exchange exchange) {
        PipelineRun run = exchange.getProperty(PROP_PIPELINE_RUN, PipelineRun.class);
        return BenchPlaceOrderResponse.builder()
                .orderId(run.getOrderId())
                .orchestration("CAMEL_PIPELINE")
                .status("IN_PROGRESS")
                .message("Camel pipeline started — orchestration via Kafka + Camel EIP")
                .build();
    }

    /**
     * Correlates incoming order-created event with existing pipeline_runs row.
     */
    public void attachPipelineRun(Exchange exchange) {
        OrderCreatedEvent event = exchange.getMessage().getBody(OrderCreatedEvent.class);
        PipelineRun run = pipelineRunRepository.findByOrderId(event.getOrderId())
                .orElseThrow(() -> new IllegalStateException("Pipeline run not found for order " + event.getOrderId()));
        exchange.setProperty(PROP_PIPELINE_RUN, run);
    }

    /**
     * Generic correlation helper for events that carry only orderId.
     */
    public void attachPipelineRunFromOrderId(Exchange exchange) {
        UUID orderId = extractOrderId(exchange);
        PipelineRun run = pipelineRunRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException("Pipeline run not found for order " + orderId));
        exchange.setProperty(PROP_PIPELINE_RUN, run);
    }

    /**
     * Rehydrates persisted line items so route steps can build follow-up commands.
     */
    public List<OrderLineItem> extractLineItems(Exchange exchange) {
        PipelineRun run = exchange.getProperty(PROP_PIPELINE_RUN, PipelineRun.class);
        return readItems(run.getPayload());
    }

    /**
     * Guard used inside Camel split() block.
     */
    public OrderLineItem validateLineItem(OrderLineItem item) {
        if (item.getProductId() == null || item.getQuantity() == null || item.getQuantity() < 1) {
            throw new IllegalArgumentException("Invalid line item");
        }
        return item;
    }

    /**
     * Builds reserve-stock command after create-order event.
     */
    public ReserveStockCommand buildReserveCommand(Exchange exchange) {
        PipelineRun run = exchange.getProperty(PROP_PIPELINE_RUN, PipelineRun.class);
        return ReserveStockCommand.builder()
                .orderId(run.getOrderId())
                .items(readItems(run.getPayload()))
                .build();
    }

    /**
     * Builds confirm-order command after stock-reserved event.
     */
    public OrderConfirmCommand buildConfirmCommand(Exchange exchange) {
        PipelineRun run = exchange.getProperty(PROP_PIPELINE_RUN, PipelineRun.class);
        return OrderConfirmCommand.builder().orderId(run.getOrderId()).build();
    }

    /**
     * Compensation command for failed reservation branch.
     */
    public OrderCancelCommand buildCancelCommand(Exchange exchange) {
        StockReservationFailedEvent event = exchange.getMessage().getBody(StockReservationFailedEvent.class);
        return OrderCancelCommand.builder()
                .orderId(event.getOrderId())
                .reason(event.getReason())
                .build();
    }

    /**
     * Companion compensation command for failed reservation branch.
     */
    public ReleaseStockCommand buildReleaseCommand(Exchange exchange) {
        StockReservationFailedEvent event = exchange.getMessage().getBody(StockReservationFailedEvent.class);
        return ReleaseStockCommand.builder().orderId(event.getOrderId()).build();
    }

    /**
     * Builds fulfillment command after order confirmation.
     */
    public StartFulfillmentCommand buildFulfillmentCommand(Exchange exchange) {
        PipelineRun run = exchange.getProperty(PROP_PIPELINE_RUN, PipelineRun.class);
        return StartFulfillmentCommand.builder()
                .orderId(run.getOrderId())
                .items(readItems(run.getPayload()))
                .build();
    }

    /**
     * Marks B-path success and computes elapsedMs for benchmark status endpoint.
     */
    public void markPipelineCompleted(Exchange exchange) {
        ShipmentUpdatedEvent event = exchange.getMessage().getBody(ShipmentUpdatedEvent.class);
        pipelineRunRepository.findByOrderId(event.getOrderId()).ifPresent(run -> {
            run.setStatus("COMPLETED");
            run.setTrackingNumber(event.getTrackingNumber());
            run.setCompletedAt(Instant.now());
            if (run.getCreatedAt() != null) {
                run.setElapsedMs(Instant.now().toEpochMilli() - run.getCreatedAt().toEpochMilli());
            }
            pipelineRunRepository.save(run);
        });
    }

    /**
     * Marks B-path failure and computes elapsedMs for benchmark status endpoint.
     */
    public void markPipelineFailed(Exchange exchange) {
        StockReservationFailedEvent event = exchange.getMessage().getBody(StockReservationFailedEvent.class);
        pipelineRunRepository.findByOrderId(event.getOrderId()).ifPresent(run -> {
            run.setStatus("FAILED");
            run.setCompletedAt(Instant.now());
            if (run.getCreatedAt() != null) {
                run.setElapsedMs(Instant.now().toEpochMilli() - run.getCreatedAt().toEpochMilli());
            }
            pipelineRunRepository.save(run);
        });
    }

    /**
     * Extracts orderId from supported event types used by Camel-heavy routes.
     */
    private UUID extractOrderId(Exchange exchange) {
        Object body = exchange.getMessage().getBody();
        if (body instanceof StockReservedEvent e) {
            return e.getOrderId();
        }
        if (body instanceof OrderCreatedEvent e) {
            return e.getOrderId();
        }
        if (body instanceof com.integrationservice.messaging.OrderConfirmedEvent e) {
            return e.getOrderId();
        }
        throw new IllegalStateException("Cannot extract orderId from " + body.getClass().getSimpleName());
    }

    /**
     * Persists request payload for later route steps.
     */
    private String writeItems(List<PlaceOrderItemDto> items) {
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize pipeline items", e);
        }
    }

    /**
     * Rehydrates saved payload and converts API DTOs to messaging line items.
     */
    private List<OrderLineItem> readItems(String payload) {
        try {
            List<PlaceOrderItemDto> items = objectMapper.readValue(payload, new TypeReference<>() {
            });
            return items.stream()
                    .map(item -> OrderLineItem.builder()
                            .productId(item.getProductId())
                            .quantity(item.getQuantity())
                            .build())
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize pipeline items", e);
        }
    }
}
