package com.notificationservice.service.impl;

import com.notificationservice.dto.SendNotificationCommandDto;
import com.notificationservice.entity.NotificationLog;
import com.notificationservice.enums.NotificationChannel;
import com.notificationservice.enums.NotificationStatus;
import com.notificationservice.repository.NotificationLogRepository;
import com.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogRepository notificationLogRepository;

    @Override
    @Transactional
    public void sendNotification(SendNotificationCommandDto command) {
        String message = command.getMessage() != null
                ? command.getMessage()
                : "Order update for order " + command.getOrderId();

        log.info("[NOTIFICATION-STUB] type={} orderId={} message={}",
                command.getNotificationType(), command.getOrderId(), message);

        notificationLogRepository.save(NotificationLog.builder()
                .id(UUID.randomUUID())
                .orderId(command.getOrderId())
                .channel(NotificationChannel.EMAIL)
                .message(message)
                .status(NotificationStatus.SENT)
                .build());
    }
}
