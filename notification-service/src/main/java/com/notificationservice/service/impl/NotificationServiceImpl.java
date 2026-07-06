package com.notificationservice.service.impl;

import com.notificationservice.dto.SendNotificationCommandDto;
import com.notificationservice.entity.NotificationLog;
import com.notificationservice.enums.NotificationChannel;
import com.notificationservice.enums.NotificationStatus;
import com.notificationservice.repository.NotificationLogRepository;
import com.notificationservice.service.NotificationRateLimitService;
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
    private final NotificationRateLimitService notificationRateLimitService;

    @Override
    @Transactional
    public void sendNotification(SendNotificationCommandDto command) {
        String notificationType = command.getNotificationType() != null
                ? command.getNotificationType()
                : "ORDER_UPDATE";

        if (NotificationRateLimitServiceImpl.SEARCH_INTEREST_TYPE.equals(notificationType)
                && command.getCustomerId() != null) {
            var eligibility = notificationRateLimitService.checkEligibility(
                    command.getCustomerId(), notificationType);
            if (!eligibility.isEligible()) {
                log.info("[NOTIFICATION-SKIPPED] type={} customerId={} reason=rate-limit sent={}/{} in {}h",
                        notificationType, command.getCustomerId(),
                        eligibility.getSentInWindow(), eligibility.getMaxAllowed(), eligibility.getWindowHours());
                return;
            }
        }

        String message = command.getMessage() != null
                ? command.getMessage()
                : "Order update for order " + command.getOrderId();

        log.info("[NOTIFICATION-STUB] type={} customerId={} orderId={} email={} message={}",
                notificationType, command.getCustomerId(), command.getOrderId(),
                command.getRecipientEmail(), message);

        notificationLogRepository.save(NotificationLog.builder()
                .id(UUID.randomUUID())
                .orderId(command.getOrderId())
                .customerId(command.getCustomerId())
                .interestId(command.getInterestId())
                .notificationType(notificationType)
                .recipientEmail(command.getRecipientEmail())
                .channel(NotificationChannel.EMAIL)
                .message(message)
                .status(NotificationStatus.SENT)
                .build());
    }
}
