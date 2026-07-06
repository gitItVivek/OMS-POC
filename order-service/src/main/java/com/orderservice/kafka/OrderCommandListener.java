package com.orderservice.kafka;

import tools.jackson.databind.ObjectMapper;
import com.orderservice.dto.OrderCancelCommandDto;
import com.orderservice.dto.OrderConfirmCommandDto;
import com.orderservice.dto.OrderCreateCommandDto;
import com.orderservice.service.OrderCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCommandListener {

    private final OrderCommandService orderCommandService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OmsKafkaTopics.ORDER_CREATE_COMMAND, groupId = "order-service")
    public void onCreateCommand(String payload) throws Exception {
        OrderCreateCommandDto command = objectMapper.readValue(payload, OrderCreateCommandDto.class);
        log.info("Received order create command for orderId={}", command.getOrderId());
        orderCommandService.handleCreateCommand(command);
    }

    @KafkaListener(topics = OmsKafkaTopics.ORDER_CONFIRM_COMMAND, groupId = "order-service")
    public void onConfirmCommand(String payload) throws Exception {
        OrderConfirmCommandDto command = objectMapper.readValue(payload, OrderConfirmCommandDto.class);
        log.info("Received order confirm command for orderId={}", command.getOrderId());
        orderCommandService.handleConfirmCommand(command);
    }

    @KafkaListener(topics = OmsKafkaTopics.ORDER_CANCEL_COMMAND, groupId = "order-service")
    public void onCancelCommand(String payload) throws Exception {
        OrderCancelCommandDto command = objectMapper.readValue(payload, OrderCancelCommandDto.class);
        log.info("Received order cancel command for orderId={}", command.getOrderId());
        orderCommandService.handleCancelCommand(command);
    }
}
