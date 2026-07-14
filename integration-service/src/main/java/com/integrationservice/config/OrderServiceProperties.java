package com.integrationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.service")
public record OrderServiceProperties(String baseUrl) {
}
