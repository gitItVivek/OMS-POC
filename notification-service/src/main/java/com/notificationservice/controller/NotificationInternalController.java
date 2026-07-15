package com.notificationservice.controller;

import com.notificationservice.dto.SendNotificationRequestDto;
import com.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/notifications")
@RequiredArgsConstructor
public class NotificationInternalController {

    private final NotificationService notificationService;

    @PostMapping
    public void send(@RequestBody SendNotificationRequestDto request) {
        notificationService.sendOrderNotification(request);
    }
}
