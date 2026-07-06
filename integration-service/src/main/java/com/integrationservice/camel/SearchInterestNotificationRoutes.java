package com.integrationservice.camel;

import com.integrationservice.kafka.OmsKafkaTopics;
import com.integrationservice.messaging.SearchInterestRegisteredEvent;
import com.integrationservice.messaging.SendNotificationCommand;
import com.integrationservice.service.SearchInterestNotificationProcessor;
import com.integrationservice.service.impl.SearchInterestNotificationProcessorImpl;
import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchInterestNotificationRoutes extends RouteBuilder {

    private final SearchInterestNotificationProcessor searchInterestNotificationProcessor;

    @Override
    public void configure() {
        from("kafka:" + OmsKafkaTopics.SEARCH_INTEREST_REGISTERED_EVENT + "?groupId=integration-search-interest")
                .routeId("search-interest-notify")
                .unmarshal().json(SearchInterestRegisteredEvent.class)
                .bean(searchInterestNotificationProcessor, "evaluateRateLimit")
                .choice()
                    .when(header(SearchInterestNotificationProcessorImpl.HEADER_NOTIFICATION_ALLOWED).isEqualTo(true))
                        .bean(searchInterestNotificationProcessor, "buildNotificationCommand")
                        .bean(searchInterestNotificationProcessor, "toCommand")
                        .marshal().json(SendNotificationCommand.class)
                        .to("kafka:" + OmsKafkaTopics.NOTIFICATION_SEND_COMMAND)
                    .otherwise()
                        .log("Skipping search-interest email — rate limit reached (max 2 per 72h)")
                .end();
    }
}
