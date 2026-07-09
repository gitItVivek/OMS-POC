package com.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEligibilityResponseDto {

    private boolean eligible;
    private int sentInWindow;
    private int maxAllowed;
    private int windowHours;
}
