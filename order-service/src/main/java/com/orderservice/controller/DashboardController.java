package com.orderservice.controller;

import com.orderservice.dto.DashboardResponseDto;
import com.orderservice.enums.TrendWindow;
import com.orderservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public DashboardResponseDto dashboard(
            @RequestParam(defaultValue = "WEEKLY") TrendWindow period) {
        return dashboardService.getDashboard(period);
    }
}
