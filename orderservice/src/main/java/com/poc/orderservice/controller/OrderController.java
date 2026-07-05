package com.poc.orderservice.controller;

import com.poc.orderservice.dto.ApiResponseDto;
import com.poc.orderservice.dto.CreateOrderRequestDto;
import com.poc.orderservice.dto.OrderResponseDto;
import com.poc.orderservice.dto.OrderSearchRequestDto;
import com.poc.orderservice.dto.SubmitOrderRequestDto;
import com.poc.orderservice.dto.UpdateOrderRequestDto;
import com.poc.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping(value = "/orders",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto> createOrder(
            @Valid @RequestBody CreateOrderRequestDto createOrderRequestDto) {
        OrderResponseDto response = orderService.createOrder(createOrderRequestDto);
        ApiResponseDto apiResponseDto = new ApiResponseDto(
                HttpStatus.CREATED.value(),
                HttpStatus.CREATED.getReasonPhrase(),
                "Order created successfully",
                response);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponseDto);
    }

    @GetMapping(value = "/orders/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getOrderById(@PathVariable Long id) {
        OrderResponseDto response = orderService.getOrderById(id);
        ApiResponseDto apiResponseDto = new ApiResponseDto(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                "Order fetched successfully",
                response
        );
        return ResponseEntity.ok().body(apiResponseDto);
    }

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto> searchOrders(@Valid OrderSearchRequestDto orderSearchRequestDto) {
        Page<OrderResponseDto> response = orderService.searchOrder(orderSearchRequestDto);
        ApiResponseDto apiResponseDto = new ApiResponseDto(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                "Orders fetched successfully",
                response);
        return ResponseEntity.ok(apiResponseDto);
    }

    @PatchMapping(value = "/orders/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequestDto updateOrderRequestDto) {
        OrderResponseDto response = orderService.updateOrder(id, updateOrderRequestDto);
        ApiResponseDto apiResponseDto = new ApiResponseDto(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                "Order updated successfully",
                response);
        return ResponseEntity.ok(apiResponseDto);
    }

    @DeleteMapping(value = "/orders/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto> deleteOrder(
            @PathVariable Long id,
            @RequestParam Long deletedBy) {
        orderService.deleteOrder(id, deletedBy);
        ApiResponseDto apiResponseDto = new ApiResponseDto(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                "Order deleted successfully",
                Boolean.TRUE);
        return ResponseEntity.ok(apiResponseDto);
    }

    @PostMapping(value = "/orders/{id}/submit",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto> submitOrder(
            @PathVariable Long id,
            @Valid @RequestBody SubmitOrderRequestDto submitOrderRequestDto) {
        OrderResponseDto response = orderService.submitOrder(id, submitOrderRequestDto);
        ApiResponseDto apiResponseDto = new ApiResponseDto(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                "Order submitted successfully",
                response);
        return ResponseEntity.ok(apiResponseDto);
    }
}

