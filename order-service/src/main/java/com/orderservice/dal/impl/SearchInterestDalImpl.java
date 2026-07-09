package com.orderservice.dal.impl;

import com.orderservice.dal.SearchInterestDal;
import com.orderservice.entity.ProductSearchTrend;
import com.orderservice.entity.SearchInterest;
import com.orderservice.enums.TrendWindow;
import com.orderservice.repository.ProductSearchTrendRepository;
import com.orderservice.repository.SearchInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SearchInterestDalImpl implements SearchInterestDal {

    private final SearchInterestRepository searchInterestRepository;
    private final ProductSearchTrendRepository productSearchTrendRepository;

    @Override
    @Transactional
    public void saveAllInterests(List<SearchInterest> interests) {
        searchInterestRepository.saveAll(interests);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SearchInterest> findRecentInterests(UUID customerId, int limit) {
        return searchInterestRepository.findByCustomerIdOrderByCreatedAtDesc(
                customerId, PageRequest.of(0, limit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctCategories(UUID customerId, int limit) {
        return searchInterestRepository.findDistinctCategoriesByCustomerId(
                customerId, PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    public void incrementTrend(UUID productId, TrendWindow trendWindow, LocalDate windowStart) {
        ProductSearchTrend trend = productSearchTrendRepository
                .findByProductIdAndTrendWindowAndWindowStart(productId, trendWindow, windowStart)
                .orElseGet(() -> ProductSearchTrend.builder()
                        .id(UUID.randomUUID())
                        .productId(productId)
                        .trendWindow(trendWindow)
                        .windowStart(windowStart)
                        .searchCount(0)
                        .build());

        trend.setSearchCount(trend.getSearchCount() + 1);
        productSearchTrendRepository.save(trend);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasInterestForKey(UUID customerId, String interestKey) {
        if (interestKey == null || interestKey.isBlank()) {
            return false;
        }
        return searchInterestRepository.existsByCustomerIdAndInterestKey(customerId, interestKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSearchTrend> findTopTrending(TrendWindow trendWindow, LocalDate windowStart, int limit) {
        return productSearchTrendRepository.findByTrendWindowAndWindowStartOrderBySearchCountDesc(
                trendWindow, windowStart, PageRequest.of(0, limit));
    }
}
