package com.integrationservice.service;

import com.integrationservice.dto.PlaceOrderRequestDto;
import com.integrationservice.dto.PlaceOrderResponseDto;

public interface SyncPlaceOrderOrchestrator {

    PlaceOrderResponseDto placeOrder(PlaceOrderRequestDto request);
}
