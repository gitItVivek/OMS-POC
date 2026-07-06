package com.notificationservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "oms.notification.search-interest")
public class SearchInterestNotificationProperties {

    private int maxEmails = 2;
    private int windowHours = 72;
}
