package com.notificationservice.service.impl;

import com.notificationservice.dto.SendNotificationRequestDto;
import com.notificationservice.entity.NotificationLog;
import com.notificationservice.enums.NotificationChannel;
import com.notificationservice.enums.NotificationStatus;
import com.notificationservice.repository.NotificationLogRepository;
import com.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    @Override
    @Transactional
    public void sendOrderNotification(SendNotificationRequestDto request) {
        notificationLogRepository.save(NotificationLog.builder()
                .id(UUID.randomUUID())
                .orderId(request.getOrderId())
                .channel(NotificationChannel.EMAIL)
                .message(request.getMessage())
                .status(NotificationStatus.SENT)
                .build());
    }
}
