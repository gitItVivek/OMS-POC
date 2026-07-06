package com.orderservice.config;

import com.orderservice.web.IdentityAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final IdentityAuthInterceptor identityAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(identityAuthInterceptor)
                .addPathPatterns(
                        "/orders",
                        "/orders/**",
                        "/users/*/orders",
                        "/api/search",
                        "/api/dashboard");
    }
}
