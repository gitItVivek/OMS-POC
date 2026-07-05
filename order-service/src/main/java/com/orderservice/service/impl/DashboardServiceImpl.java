package com.orderservice.service.impl;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dal.SearchInterestDal;
import com.orderservice.dto.DashboardResponseDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.entity.ProductSearchTrend;
import com.orderservice.entity.SearchInterest;
import com.orderservice.enums.DashboardSource;
import com.orderservice.enums.TrendWindow;
import com.orderservice.mapper.ProductSummaryMapper;
import com.orderservice.service.DashboardService;
import com.orderservice.util.TrendWindowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int DASHBOARD_PRODUCT_LIMIT = 10;
    private static final int HISTORY_LOOKBACK = 50;
    private static final int CATEGORY_LOOKBACK = 5;

    private final SearchInterestDal searchInterestDal;
    private final InventoryServiceClient inventoryServiceClient;
    private final ProductSummaryMapper productSummaryMapper;

    @Override
    public DashboardResponseDto getDashboard(UUID customerId, TrendWindow period) {
        List<SearchInterest> recentInterests = searchInterestDal.findRecentInterests(customerId, HISTORY_LOOKBACK);
        List<String> categories = searchInterestDal.findDistinctCategories(customerId, CATEGORY_LOOKBACK);

        if (!recentInterests.isEmpty()) {
            return buildPersonalizedDashboard(recentInterests, categories);
        }

        return buildTrendingDashboard(period);
    }

    private DashboardResponseDto buildPersonalizedDashboard(
            List<SearchInterest> recentInterests,
            List<String> categories) {

        Set<UUID> productIds = new LinkedHashSet<>();
        recentInterests.forEach(interest -> productIds.add(interest.getProductId()));

        List<ProductSummaryDto> products = productSummaryMapper.deduplicateByProductId(
                inventoryServiceClient.getProductsByIds(List.copyOf(productIds)));

        if (products.size() < DASHBOARD_PRODUCT_LIMIT && !categories.isEmpty()) {
            List<ProductSummaryDto> expanded = new ArrayList<>(products);
            for (String category : categories) {
                if (expanded.size() >= DASHBOARD_PRODUCT_LIMIT) {
                    break;
                }
                expanded.addAll(inventoryServiceClient
                        .getProductsByCategory(category, 0, DASHBOARD_PRODUCT_LIMIT)
                        .getItems());
            }
            products = productSummaryMapper.deduplicateByProductId(expanded)
                    .stream()
                    .limit(DASHBOARD_PRODUCT_LIMIT)
                    .toList();
        } else {
            products = products.stream().limit(DASHBOARD_PRODUCT_LIMIT).toList();
        }

        return DashboardResponseDto.builder()
                .source(DashboardSource.PERSONALIZED)
                .period(null)
                .categoriesFromHistory(categories)
                .products(products)
                .build();
    }

    private DashboardResponseDto buildTrendingDashboard(TrendWindow period) {
        LocalDate windowStart = TrendWindowUtils.windowStart(period, LocalDate.now());
        List<ProductSearchTrend> trends = searchInterestDal.findTopTrending(
                period, windowStart, DASHBOARD_PRODUCT_LIMIT);

        List<UUID> productIds = trends.stream().map(ProductSearchTrend::getProductId).toList();
        List<ProductSummaryDto> products = inventoryServiceClient.getProductsByIds(productIds);

        return DashboardResponseDto.builder()
                .source(DashboardSource.TRENDING)
                .period(period)
                .categoriesFromHistory(List.of())
                .products(products)
                .build();
    }
}
