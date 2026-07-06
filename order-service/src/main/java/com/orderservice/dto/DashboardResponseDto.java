package com.orderservice.dto;

import com.orderservice.enums.DashboardSource;
import com.orderservice.enums.TrendWindow;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {

    private DashboardSource source;
    private TrendWindow period;
    private List<String> categoriesFromHistory;
    private List<ProductSummaryDto> products;
}
