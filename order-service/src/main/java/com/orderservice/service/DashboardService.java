package com.orderservice.service;

import com.orderservice.dto.DashboardResponseDto;
import com.orderservice.enums.TrendWindow;

public interface DashboardService {

    DashboardResponseDto getDashboard(TrendWindow period);
}
