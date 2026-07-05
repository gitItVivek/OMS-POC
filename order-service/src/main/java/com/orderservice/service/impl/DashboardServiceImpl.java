package com.orderservice.service.impl;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dal.SearchInterestDal;
import com.orderservice.dto.DashboardResponseDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.entity.ProductSearchTrend;
import com.orderservice.entity.SearchInterest;
import com.orderservice.enums.DashboardSource;
import com.orderservice.enums.TrendWindow;
import com.orderservice.config.OrderExperienceProperties;
import com.orderservice.mapper.ProductSummaryMapper;
import com.orderservice.security.AuthContext;
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

    private final SearchInterestDal searchInterestDal;
    private final InventoryServiceClient inventoryServiceClient;
    private final ProductSummaryMapper productSummaryMapper;
    private final OrderExperienceProperties orderExperienceProperties;

    @Override
    public DashboardResponseDto getDashboard(TrendWindow period) {
        UUID userId = AuthContext.currentUserId();
        int historyLookback = orderExperienceProperties.getDashboard().getHistoryLookback();
        int categoryLookback = orderExperienceProperties.getDashboard().getCategoryLookback();
        List<SearchInterest> recentInterests = searchInterestDal.findRecentInterests(userId, historyLookback);
        List<String> categories = searchInterestDal.findDistinctCategories(userId, categoryLookback);

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

        int productLimit = orderExperienceProperties.getDashboard().getProductLimit();

        if (products.size() < productLimit && !categories.isEmpty()) {
            List<ProductSummaryDto> expanded = new ArrayList<>(products);
            for (String category : categories) {
                if (expanded.size() >= productLimit) {
                    break;
                }
                expanded.addAll(inventoryServiceClient
                        .getProductsByCategory(category, 0, productLimit)
                        .getItems());
            }
            products = productSummaryMapper.deduplicateByProductId(expanded)
                    .stream()
                    .limit(productLimit)
                    .toList();
        } else {
            products = products.stream().limit(productLimit).toList();
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
                period, windowStart, orderExperienceProperties.getDashboard().getProductLimit());

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
