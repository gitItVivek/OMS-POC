package com.integrationservice.config;

import com.integrationservice.client.FulfillmentServiceClient;
import com.integrationservice.client.InventoryServiceClient;
import com.integrationservice.client.NotificationServiceClient;
import com.integrationservice.client.OrderServiceClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({
        OrderServiceProperties.class,
        InventoryServiceProperties.class,
        FulfillmentServiceProperties.class,
        NotificationServiceProperties.class
})
public class ServiceClientConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    OrderServiceClient orderServiceClient(RestClient.Builder builder, OrderServiceProperties properties) {
        return new OrderServiceClient(builder.baseUrl(properties.baseUrl()).build());
    }

    @Bean
    InventoryServiceClient inventoryServiceClient(RestClient.Builder builder, InventoryServiceProperties properties) {
        return new InventoryServiceClient(builder.baseUrl(properties.baseUrl()).build());
    }

    @Bean
    FulfillmentServiceClient fulfillmentServiceClient(RestClient.Builder builder, FulfillmentServiceProperties properties) {
        return new FulfillmentServiceClient(builder.baseUrl(properties.baseUrl()).build());
    }

    @Bean
    NotificationServiceClient notificationServiceClient(RestClient.Builder builder, NotificationServiceProperties properties) {
        return new NotificationServiceClient(builder.baseUrl(properties.baseUrl()).build());
    }
}
