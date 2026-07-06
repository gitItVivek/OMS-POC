package com.notificationservice.kafka;

import tools.jackson.databind.ObjectMapper;
import com.notificationservice.dto.SendNotificationCommandDto;
import com.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCommandListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = OmsKafkaTopics.NOTIFICATION_SEND_COMMAND, groupId = "notification-service")
    public void onSendNotification(String payload) throws Exception {
        SendNotificationCommandDto command = objectMapper.readValue(payload, SendNotificationCommandDto.class);
        log.info("Received notification command for orderId={}", command.getOrderId());
        notificationService.sendNotification(command);
    }
}
