package com.inventoryservice.repository;

import com.inventoryservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))
            """)
    Page<Product> search(@Param("query") String query, Pageable pageable);

    List<Product> findByIdIn(Collection<UUID> ids);

    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))
            """)
    Page<Product> findByCategoryContaining(@Param("category") String category, Pageable pageable);
}
