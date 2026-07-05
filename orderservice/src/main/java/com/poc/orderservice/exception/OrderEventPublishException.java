package com.poc.orderservice.exception;

public class OrderEventPublishException extends RuntimeException {

    public OrderEventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
