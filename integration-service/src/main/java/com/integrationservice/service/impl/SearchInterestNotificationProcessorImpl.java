package com.integrationservice.service.impl;

import com.integrationservice.client.IdentityUserContactClient;
import com.integrationservice.client.NotificationRateLimitClient;
import com.integrationservice.dto.IdentityUserContactDto;
import com.integrationservice.dto.NotificationEligibilityDto;
import com.integrationservice.messaging.SearchInterestRegisteredEvent;
import com.integrationservice.messaging.SendNotificationCommand;
import com.integrationservice.service.SearchInterestNotificationProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchInterestNotificationProcessorImpl implements SearchInterestNotificationProcessor {

    public static final String HEADER_NOTIFICATION_ALLOWED = "notificationAllowed";
    public static final String HEADER_USER_CONTACT = "userContact";
    public static final String NOTIFICATION_TYPE = "SEARCH_INTEREST";

    private final NotificationRateLimitClient notificationRateLimitClient;
    private final IdentityUserContactClient identityUserContactClient;

    @Override
    public void evaluateRateLimit(Exchange exchange) {
        SearchInterestRegisteredEvent event = exchange.getMessage().getBody(SearchInterestRegisteredEvent.class);
        NotificationEligibilityDto eligibility = notificationRateLimitClient.checkEligibility(
                event.getCustomerId(), NOTIFICATION_TYPE);

        exchange.getMessage().setHeader(HEADER_NOTIFICATION_ALLOWED, eligibility.eligible());
        log.info("Search interest rate-limit check customerId={} eligible={} sent={}/{} window={}h",
                event.getCustomerId(), eligibility.eligible(),
                eligibility.sentInWindow(), eligibility.maxAllowed(), eligibility.windowHours());
    }

    @Override
    public void buildNotificationCommand(Exchange exchange) {
        SearchInterestRegisteredEvent event = exchange.getMessage().getBody(SearchInterestRegisteredEvent.class);
        IdentityUserContactDto contact = identityUserContactClient.getUserContact(event.getCustomerId());
        exchange.getMessage().setHeader(HEADER_USER_CONTACT, contact);
    }

    @Override
    public SendNotificationCommand toCommand(Exchange exchange) {
        SearchInterestRegisteredEvent event = exchange.getMessage().getBody(SearchInterestRegisteredEvent.class);
        IdentityUserContactDto contact = exchange.getMessage().getHeader(HEADER_USER_CONTACT, IdentityUserContactDto.class);

        String displayName = contact != null && contact.displayName() != null
                ? contact.displayName()
                : "there";
        String productTitle = event.getProductTitle() != null ? event.getProductTitle() : "a product";
        String searchQuery = event.getSearchQuery() != null ? event.getSearchQuery() : event.getInterestKey();

        String message = "Hi " + displayName + ", you searched for \"" + searchQuery
                + "\". We thought you'd like \"" + productTitle + "\""
                + (event.getCategory() != null ? " in " + event.getCategory() : "")
                + ". Browse similar items on OMS-POC.";

        return SendNotificationCommand.builder()
                .customerId(event.getCustomerId())
                .interestId(event.getInterestId())
                .recipientEmail(contact != null ? contact.email() : null)
                .notificationType(NOTIFICATION_TYPE)
                .message(message)
                .build();
    }
}
