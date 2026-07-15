package com.orderservice.web;

import com.orderservice.client.IdentityServiceClient;
import com.orderservice.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdentityAuthInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ID = "authenticatedUserId";

    private final IdentityServiceClient identityServiceClient;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            UUID userId = identityServiceClient.resolveUserId(request.getHeader("Authorization"));
            request.setAttribute(AUTHENTICATED_USER_ID, userId);
            return true;
        } catch (UnauthorizedException ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":401,\"message\":\"" + ex.getMessage().replace("\"", "'") + "\"}");
            return false;
        }
    }
}
