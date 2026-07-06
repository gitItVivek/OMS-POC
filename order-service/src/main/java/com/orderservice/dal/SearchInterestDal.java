package com.orderservice.dal;

import com.orderservice.entity.ProductSearchTrend;
import com.orderservice.entity.SearchInterest;
import com.orderservice.enums.TrendWindow;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SearchInterestDal {

    void saveAllInterests(List<SearchInterest> interests);

    List<SearchInterest> findRecentInterests(UUID customerId, int limit);

    List<String> findDistinctCategories(UUID customerId, int limit);

    void incrementTrend(UUID productId, TrendWindow trendWindow, LocalDate windowStart);

    boolean hasInterestForCategory(UUID customerId, String category);

    List<ProductSearchTrend> findTopTrending(TrendWindow trendWindow, LocalDate windowStart, int limit);
}
