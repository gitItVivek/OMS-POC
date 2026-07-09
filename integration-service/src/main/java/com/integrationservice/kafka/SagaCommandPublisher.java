package com.integrationservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaCommandPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publish(String topic, UUID orderId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(topic, orderId.toString(), json);
            log.info("Published to topic={} orderId={}", topic, orderId);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to publish Kafka message to topic '" + topic
                            + "'. Is Kafka running? Start it with: docker compose up -d",
                    e);
        }
    }
}
