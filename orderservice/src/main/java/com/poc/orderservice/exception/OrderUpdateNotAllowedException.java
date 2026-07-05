package com.poc.orderservice.exception;

public class OrderUpdateNotAllowedException extends RuntimeException {

    public OrderUpdateNotAllowedException(String message) {
        super(message);
    }
}
