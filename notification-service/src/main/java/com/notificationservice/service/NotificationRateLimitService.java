package com.notificationservice.service;

import com.notificationservice.dto.NotificationEligibilityResponseDto;

import java.util.UUID;

public interface NotificationRateLimitService {

    NotificationEligibilityResponseDto checkEligibility(UUID customerId, String notificationType);
}
