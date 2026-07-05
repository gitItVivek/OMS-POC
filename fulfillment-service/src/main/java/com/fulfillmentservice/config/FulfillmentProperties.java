package com.fulfillmentservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "fulfillment")
@Getter
@Setter
public class FulfillmentProperties {

    private String defaultCarrier;
    private String trackingNumberPrefix;
    private int trackingNumberMin;
    private int trackingNumberMax;
}
