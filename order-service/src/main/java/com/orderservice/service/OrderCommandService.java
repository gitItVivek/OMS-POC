package com.orderservice.service;

import com.orderservice.dto.OrderCancelCommandDto;
import com.orderservice.dto.OrderConfirmCommandDto;
import com.orderservice.dto.OrderCreateCommandDto;

public interface OrderCommandService {

    void handleCreateCommand(OrderCreateCommandDto command);

    void handleConfirmCommand(OrderConfirmCommandDto command);

    void handleCancelCommand(OrderCancelCommandDto command);
}
