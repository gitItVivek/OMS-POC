package com.integrationservice.web;

import com.integrationservice.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public final class RequestAuthContext {

    private RequestAuthContext() {
    }

    public static UUID currentUserId() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("Authentication required. Login via identity-service.");
        }
        HttpServletRequest request = attributes.getRequest();
        Object userId = request.getAttribute(IdentityAuthInterceptor.AUTHENTICATED_USER_ID);
        if (!(userId instanceof UUID uuid)) {
            throw new UnauthorizedException("Authentication required. Login via identity-service.");
        }
        return uuid;
    }
}
