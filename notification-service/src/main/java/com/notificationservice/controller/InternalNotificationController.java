package com.notificationservice.controller;

import com.notificationservice.dto.NotificationEligibilityResponseDto;
import com.notificationservice.service.NotificationRateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationRateLimitService notificationRateLimitService;

    @GetMapping("/eligibility")
    public NotificationEligibilityResponseDto eligibility(
            @RequestParam UUID customerId,
            @RequestParam String notificationType) {
        return notificationRateLimitService.checkEligibility(customerId, notificationType);
    }
}
