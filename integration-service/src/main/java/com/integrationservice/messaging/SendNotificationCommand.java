package com.integrationservice.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationCommand {

    private UUID orderId;
    private UUID customerId;
    private UUID interestId;
    private String recipientEmail;
    private String notificationType;
    private String message;
}
