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
public class InventoryCommandListener {

    private final StockReservationService stockReservationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OmsKafkaTopics.INVENTORY_RESERVE_COMMAND, groupId = "inventory-service")
    public void onReserveCommand(String payload) throws Exception {
        ReserveStockCommandDto command = objectMapper.readValue(payload, ReserveStockCommandDto.class);
        log.info("Received reserve stock command for orderId={}", command.getOrderId());
        stockReservationService.reserveStock(command);
    }

    @KafkaListener(topics = OmsKafkaTopics.INVENTORY_RELEASE_COMMAND, groupId = "inventory-service")
    public void onReleaseCommand(String payload) throws Exception {
        ReleaseStockCommandDto command = objectMapper.readValue(payload, ReleaseStockCommandDto.class);
        log.info("Received release stock command for orderId={}", command.getOrderId());
        stockReservationService.releaseStock(command);
    }
}
