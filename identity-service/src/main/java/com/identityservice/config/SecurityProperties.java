package com.identityservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oms.security")
@Getter
@Setter
public class SecurityProperties {

    private int bcryptStrength;
}
