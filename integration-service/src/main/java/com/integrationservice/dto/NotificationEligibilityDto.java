package com.integrationservice.dto;

public record NotificationEligibilityDto(
        boolean eligible,
        int sentInWindow,
        int maxAllowed,
        int windowHours) {
}
