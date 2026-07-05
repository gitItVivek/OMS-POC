package com.poc.orderservice.exception;

public class OrderItemNotFoundException extends RuntimeException {

    public OrderItemNotFoundException(String message) {
        super(message);
    }
}
