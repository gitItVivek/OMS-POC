package com.fulfillmentservice.kafka;

import tools.jackson.databind.ObjectMapper;
import com.fulfillmentservice.dto.ShipmentUpdatedEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FulfillmentEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishShipmentUpdated(ShipmentUpdatedEventDto event) {
        publishShipmentUpdated(event, OmsKafkaTopics.FULFILLMENT_SHIPMENT_UPDATED_EVENT);
    }

    public void publishShipmentUpdated(ShipmentUpdatedEventDto event, String topic) {
        try {
            kafkaTemplate.send(topic, event.getOrderId().toString(), objectMapper.writeValueAsString(event));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize shipment updated event", e);
        }
    }
}
