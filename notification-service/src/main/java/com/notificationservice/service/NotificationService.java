package com.notificationservice.service;

import com.notificationservice.dto.SendNotificationCommandDto;

public interface NotificationService {

    void sendNotification(SendNotificationCommandDto command);
}
