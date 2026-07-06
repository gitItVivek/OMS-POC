package com.orderservice.repository;

import com.orderservice.entity.SearchInterest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SearchInterestRepository extends JpaRepository<SearchInterest, UUID> {

    List<SearchInterest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId, Pageable pageable);

    @Query("""
            SELECT DISTINCT s.category FROM SearchInterest s
            WHERE s.customerId = :customerId AND s.category IS NOT NULL
            """)
    List<String> findDistinctCategoriesByCustomerId(@Param("customerId") UUID customerId, Pageable pageable);

    @Query("""
            SELECT COUNT(s) > 0 FROM SearchInterest s
            WHERE s.customerId = :customerId
              AND s.category IS NOT NULL
              AND LOWER(TRIM(s.category)) = LOWER(TRIM(:category))
            """)
    boolean existsByCustomerIdAndCategory(@Param("customerId") UUID customerId, @Param("category") String category);
}
