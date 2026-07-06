package com.orderservice.entity;

import com.orderservice.enums.TrendWindow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "product_search_trends",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_product_search_trends",
                columnNames = {"product_id", "trend_window", "window_start"}
        )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchTrend {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "trend_window", nullable = false, length = 20)
    private TrendWindow trendWindow;

    @Column(name = "window_start", nullable = false)
    private LocalDate windowStart;

    @Column(name = "search_count", nullable = false)
    private Integer searchCount;
}
