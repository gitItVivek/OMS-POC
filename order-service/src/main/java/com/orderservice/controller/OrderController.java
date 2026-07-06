package com.orderservice.controller;

import com.orderservice.dto.CreateOrderRequestDto;
import com.orderservice.dto.DashboardResponseDto;
import com.orderservice.dto.OrderResponseDto;
import com.orderservice.dto.SearchResponseDto;
import com.orderservice.enums.TrendWindow;
import com.orderservice.service.DashboardService;
import com.orderservice.service.OrderService;
import com.orderservice.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final SearchService searchService;
    private final DashboardService dashboardService;

    @PostMapping("/orders")
    public OrderResponseDto createOrder(@RequestBody CreateOrderRequestDto request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/orders/{orderId}")
    public OrderResponseDto getOrder(@PathVariable UUID orderId) {
        return orderService.getOrder(orderId);
    }

    @GetMapping("/users/{userId}/orders")
    public List<OrderResponseDto> getOrdersForUser(@PathVariable UUID userId) {
        return orderService.getOrdersForUser(userId);
    }

    @GetMapping("/api/search")
    public SearchResponseDto search(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return searchService.search(query, page, size);
    }

    @GetMapping("/api/dashboard")
    public DashboardResponseDto dashboard(
            @RequestParam(defaultValue = "WEEKLY") TrendWindow period) {
        return dashboardService.getDashboard(period);
    }
}
