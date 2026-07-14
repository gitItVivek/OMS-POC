package com.integrationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.service")
public record NotificationServiceProperties(String baseUrl) {
}
