package com.orderservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oms.jwt")
@Getter
@Setter
public class JwtProperties {

    private String secret;
    private long expirySeconds;
}
