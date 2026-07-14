package com.integrationservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inventory.service")
public record InventoryServiceProperties(String baseUrl) {
}
