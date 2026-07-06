package com.orderservice.exception;

import java.util.UUID;

public class OrderAccessDeniedException extends RuntimeException {

    public OrderAccessDeniedException(UUID orderId) {
        super("Access denied for order: " + orderId);
    }

    public OrderAccessDeniedException(UUID authenticatedUserId, UUID requestedUserId) {
        super("User " + authenticatedUserId + " cannot access orders for user " + requestedUserId);
    }
}
