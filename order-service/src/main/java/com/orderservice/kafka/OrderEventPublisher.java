package com.orderservice.kafka;

import tools.jackson.databind.ObjectMapper;
import com.orderservice.dto.OrderCancelledEventDto;
import com.orderservice.dto.OrderConfirmedEventDto;
import com.orderservice.dto.OrderCreatedEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishOrderCreated(OrderCreatedEventDto event) {
        publishOrderCreated(event, OmsKafkaTopics.ORDER_CREATED_EVENT);
    }

    public void publishOrderCreated(OrderCreatedEventDto event, String topic) {
        send(topic, event.getOrderId().toString(), event);
    }

    public void publishOrderConfirmed(OrderConfirmedEventDto event) {
        publishOrderConfirmed(event, OmsKafkaTopics.ORDER_CONFIRMED_EVENT);
    }

    public void publishOrderConfirmed(OrderConfirmedEventDto event, String topic) {
        send(topic, event.getOrderId().toString(), event);
    }

    public void publishOrderCancelled(OrderCancelledEventDto event) {
        publishOrderCancelled(event, OmsKafkaTopics.ORDER_CANCELLED_EVENT);
    }

    public void publishOrderCancelled(OrderCancelledEventDto event, String topic) {
        send(topic, event.getOrderId().toString(), event);
    }

    private void send(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Kafka event for topic " + topic, e);
        }
    }
}
