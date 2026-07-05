package com.orderservice.service;

import com.orderservice.dto.DashboardResponseDto;
import com.orderservice.enums.TrendWindow;

import java.util.UUID;

public interface DashboardService {

    DashboardResponseDto getDashboard(UUID customerId, TrendWindow period);
}
