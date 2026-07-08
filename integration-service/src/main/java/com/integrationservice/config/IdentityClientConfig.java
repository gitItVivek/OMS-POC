package com.integrationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class IdentityClientConfig {

    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient identityRestClient(@Value("${identity.service.base-url}") String baseUrl) {
        return restClientBuilder()
                .baseUrl(baseUrl)
                .build();
    }
}
