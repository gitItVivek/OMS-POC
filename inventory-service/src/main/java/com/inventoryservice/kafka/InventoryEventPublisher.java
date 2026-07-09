package com.inventoryservice.kafka;

import tools.jackson.databind.ObjectMapper;
import com.inventoryservice.dto.StockReservationFailedEventDto;
import com.inventoryservice.dto.StockReservedEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void publishStockReserved(StockReservedEventDto event) {
        send(OmsKafkaTopics.INVENTORY_RESERVED_EVENT, event.getOrderId().toString(), event);
    }

    public void publishStockReservationFailed(StockReservationFailedEventDto event) {
        send(OmsKafkaTopics.INVENTORY_RESERVATION_FAILED_EVENT, event.getOrderId().toString(), event);
    }

    private void send(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize Kafka event for topic " + topic, e);
        }
    }
}
