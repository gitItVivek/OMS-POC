package com.fulfillmentservice.service;

import com.fulfillmentservice.dto.StartFulfillmentCommandDto;

public interface FulfillmentCommandService {

    void startFulfillment(StartFulfillmentCommandDto command);
}
