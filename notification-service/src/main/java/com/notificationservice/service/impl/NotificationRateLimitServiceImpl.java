package com.notificationservice.service.impl;

import com.notificationservice.config.SearchInterestNotificationProperties;
import com.notificationservice.dto.NotificationEligibilityResponseDto;
import com.notificationservice.repository.NotificationLogRepository;
import com.notificationservice.service.NotificationRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationRateLimitServiceImpl implements NotificationRateLimitService {

    public static final String SEARCH_INTEREST_TYPE = "SEARCH_INTEREST";

    private final NotificationLogRepository notificationLogRepository;
    private final SearchInterestNotificationProperties searchInterestNotificationProperties;

    @Override
    @Transactional(readOnly = true)
    public NotificationEligibilityResponseDto checkEligibility(UUID customerId, String notificationType) {
        int maxAllowed = maxAllowedFor(notificationType);
        int windowHours = windowHoursFor(notificationType);
        Instant since = Instant.now().minus(windowHours, ChronoUnit.HOURS);

        long sentInWindow = notificationLogRepository.countSentSince(customerId, notificationType, since);

        return NotificationEligibilityResponseDto.builder()
                .eligible(sentInWindow < maxAllowed)
                .sentInWindow((int) sentInWindow)
                .maxAllowed(maxAllowed)
                .windowHours(windowHours)
                .build();
    }

    private int maxAllowedFor(String notificationType) {
        if (SEARCH_INTEREST_TYPE.equals(notificationType)) {
            return searchInterestNotificationProperties.getMaxEmails();
        }
        return Integer.MAX_VALUE;
    }

    private int windowHoursFor(String notificationType) {
        if (SEARCH_INTEREST_TYPE.equals(notificationType)) {
            return searchInterestNotificationProperties.getWindowHours();
        }
        return 24;
    }
}
