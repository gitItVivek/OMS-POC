package com.orderservice.service;

import com.orderservice.dto.OrderCancelCommandDto;
import com.orderservice.dto.OrderConfirmCommandDto;
import com.orderservice.dto.OrderCreateCommandDto;
import com.orderservice.kafka.KafkaPipelineTopics;

public interface OrderCommandService {

    void handleCreateCommand(OrderCreateCommandDto command);

    void handleCreateCommand(OrderCreateCommandDto command, KafkaPipelineTopics topics);

    void handleConfirmCommand(OrderConfirmCommandDto command);

    void handleConfirmCommand(OrderConfirmCommandDto command, KafkaPipelineTopics topics);

    void handleCancelCommand(OrderCancelCommandDto command);

    void handleCancelCommand(OrderCancelCommandDto command, KafkaPipelineTopics topics);
}
