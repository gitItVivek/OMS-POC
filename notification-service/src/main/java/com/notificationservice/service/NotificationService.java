package com.notificationservice.service;

import com.notificationservice.dto.SendNotificationRequestDto;

public interface NotificationService {

    void sendOrderNotification(SendNotificationRequestDto request);
}
