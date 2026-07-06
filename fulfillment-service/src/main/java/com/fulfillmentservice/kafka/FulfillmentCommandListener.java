package com.fulfillmentservice.kafka;

import tools.jackson.databind.ObjectMapper;
import com.fulfillmentservice.dto.StartFulfillmentCommandDto;
import com.fulfillmentservice.service.FulfillmentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FulfillmentCommandListener {

    private final FulfillmentCommandService fulfillmentCommandService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OmsKafkaTopics.FULFILLMENT_START_COMMAND, groupId = "fulfillment-service")
    public void onStartFulfillment(String payload) throws Exception {
        StartFulfillmentCommandDto command = objectMapper.readValue(payload, StartFulfillmentCommandDto.class);
        log.info("Received start fulfillment command for orderId={}", command.getOrderId());
        fulfillmentCommandService.startFulfillment(command);
    }
}
