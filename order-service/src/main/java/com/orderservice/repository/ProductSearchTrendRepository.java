package com.orderservice.repository;

import com.orderservice.entity.ProductSearchTrend;
import com.orderservice.enums.TrendWindow;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductSearchTrendRepository extends JpaRepository<ProductSearchTrend, UUID> {

    Optional<ProductSearchTrend> findByProductIdAndTrendWindowAndWindowStart(
            UUID productId, TrendWindow trendWindow, LocalDate windowStart);

    List<ProductSearchTrend> findByTrendWindowAndWindowStartOrderBySearchCountDesc(
            TrendWindow trendWindow, LocalDate windowStart, Pageable pageable);
}
