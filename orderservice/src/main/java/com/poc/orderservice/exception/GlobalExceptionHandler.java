package com.poc.orderservice.exception;

import com.poc.orderservice.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponseDto> handleOrderNotFound(OrderNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OrderCreationException.class)
    public ResponseEntity<ApiResponseDto> handleOrderCreation(OrderCreationException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(OrderItemNotFoundException.class)
    public ResponseEntity<ApiResponseDto> handleOrderItemNotFound(OrderItemNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(OrderUpdateNotAllowedException.class)
    public ResponseEntity<ApiResponseDto> handleOrderUpdateNotAllowed(OrderUpdateNotAllowedException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidOrderRequestException.class)
    public ResponseEntity<ApiResponseDto> handleInvalidOrderRequest(InvalidOrderRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OrderEventPublishException.class)
    public ResponseEntity<ApiResponseDto> handleOrderEventPublish(OrderEventPublishException ex) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    private ResponseEntity<ApiResponseDto> buildResponse(HttpStatus status, String message) {
        ApiResponseDto apiResponseDto = new ApiResponseDto(
                status.value(),
                status.getReasonPhrase(),
                message,
                Boolean.FALSE);
        return ResponseEntity.status(status).body(apiResponseDto);
    }
}
