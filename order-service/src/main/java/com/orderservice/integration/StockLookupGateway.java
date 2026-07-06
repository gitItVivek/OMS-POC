package com.orderservice.integration;

import com.orderservice.dto.OrderItemRequestDto;
import com.orderservice.dto.OrderItemStockResult;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

import java.util.List;

@MessagingGateway(errorChannel = "stockLookupErrorChannel")
public interface StockLookupGateway {

    @Gateway(
            requestChannel = "stockLookupRequestChannel",
            replyChannel = "stockLookupReplyChannel",
            replyTimeout = 10000)
    List<OrderItemStockResult> lookupStock(List<OrderItemRequestDto> items);
}
