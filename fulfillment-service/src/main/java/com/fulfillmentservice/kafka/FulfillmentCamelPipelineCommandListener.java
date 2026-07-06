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
public class FulfillmentCamelPipelineCommandListener {

    private final FulfillmentCommandService fulfillmentCommandService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OmsKafkaTopics.CAMEL_FULFILLMENT_START_COMMAND, groupId = "fulfillment-service-camel")
    public void onStartFulfillment(String payload) throws Exception {
        StartFulfillmentCommandDto command = objectMapper.readValue(payload, StartFulfillmentCommandDto.class);
        log.info("[CAMEL-PIPELINE] Received fulfillment start for orderId={}", command.getOrderId());
        fulfillmentCommandService.startFulfillment(command, KafkaPipelineTopics.CAMEL);
    }
}
