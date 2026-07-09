package com.fulfillmentservice.service;

import com.fulfillmentservice.dto.StartFulfillmentCommandDto;
import com.fulfillmentservice.kafka.KafkaPipelineTopics;

public interface FulfillmentCommandService {

    void startFulfillment(StartFulfillmentCommandDto command);

    void startFulfillment(StartFulfillmentCommandDto command, KafkaPipelineTopics topics);
}
