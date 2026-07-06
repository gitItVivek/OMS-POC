package com.fulfillmentservice.service;

import com.fulfillmentservice.dto.StartFulfillmentCommandDto;

import java.util.UUID;

public interface FulfillmentBenchService {

    String startFulfillment(StartFulfillmentCommandDto command);
}
