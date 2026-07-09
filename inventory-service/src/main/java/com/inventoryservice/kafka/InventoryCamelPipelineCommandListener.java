package com.inventoryservice.kafka;

import tools.jackson.databind.ObjectMapper;
import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockCommandDto;
import com.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCamelPipelineCommandListener {

    private final StockReservationService stockReservationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OmsKafkaTopics.CAMEL_INVENTORY_RESERVE_COMMAND, groupId = "inventory-service-camel")
    public void onReserveCommand(String payload) throws Exception {
        ReserveStockCommandDto command = objectMapper.readValue(payload, ReserveStockCommandDto.class);
        log.info("[CAMEL-PIPELINE] Received reserve command for orderId={}", command.getOrderId());
        stockReservationService.reserveStock(command, KafkaPipelineTopics.CAMEL);
    }

    @KafkaListener(topics = OmsKafkaTopics.CAMEL_INVENTORY_RELEASE_COMMAND, groupId = "inventory-service-camel")
    public void onReleaseCommand(String payload) throws Exception {
        ReleaseStockCommandDto command = objectMapper.readValue(payload, ReleaseStockCommandDto.class);
        log.info("[CAMEL-PIPELINE] Received release command for orderId={}", command.getOrderId());
        stockReservationService.releaseStock(command);
    }
}
