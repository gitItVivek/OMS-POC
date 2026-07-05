package com.poc.orderservice.messaging;

import com.poc.orderservice.event.OrderSubmittedEvent;
import com.poc.orderservice.exception.OrderEventPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${orderservice.kafka.topic.order-submitted}")
    private String orderSubmittedTopic;

    public void publishOrderSubmitted(OrderSubmittedEvent event) {
        try {
            kafkaTemplate.send(orderSubmittedTopic, event.orderId().toString(), event).get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OrderEventPublishException(
                    "Interrupted while publishing order submitted event for orderId: " + event.orderId(), e);
        } catch (ExecutionException e) {
            log.error("Failed to publish order submitted event for orderId={}: {}", event.orderId(), e.getMessage(), e);
            throw new OrderEventPublishException(
                    "Failed to publish order submitted event for orderId: " + event.orderId(), e);
        }
    }
}
