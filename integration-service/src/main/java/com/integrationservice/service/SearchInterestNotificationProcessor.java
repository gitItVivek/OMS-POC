package com.integrationservice.service;

import com.integrationservice.messaging.SearchInterestRegisteredEvent;
import com.integrationservice.messaging.SendNotificationCommand;
import org.apache.camel.Exchange;

public interface SearchInterestNotificationProcessor {

    void evaluateRateLimit(Exchange exchange);

    void buildNotificationCommand(Exchange exchange);

    SendNotificationCommand toCommand(Exchange exchange);
}
