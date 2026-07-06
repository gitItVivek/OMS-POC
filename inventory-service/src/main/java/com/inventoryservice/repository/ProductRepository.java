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

    @Query(value = """
            SELECT p.id, p.sku, p.title, p.brand, p.category, p.price, p.currency, p.source_url,
                   p.available_qty, p.created_at, p.updated_at
            FROM products p
            WHERE (
                p.search_vector @@ websearch_to_tsquery('english', :query)
                OR similarity(p.title, :query) > 0.2
                OR word_similarity(:query, p.title) > 0.35
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:query), ' ', '%'), '%'))
                OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            ORDER BY (
                COALESCE(ts_rank_cd(p.search_vector, websearch_to_tsquery('english', :query)), 0)
                + GREATEST(similarity(p.title, :query), word_similarity(:query, p.title)) * 0.5
            ) DESC, p.title ASC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM products p
            WHERE (
                p.search_vector @@ websearch_to_tsquery('english', :query)
                OR similarity(p.title, :query) > 0.2
                OR word_similarity(:query, p.title) > 0.35
                OR LOWER(p.title) LIKE LOWER(CONCAT('%', REPLACE(TRIM(:query), ' ', '%'), '%'))
                OR LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            """,
            nativeQuery = true)
    Page<Product> search(@Param("query") String query, Pageable pageable);

    List<Product> findByIdIn(Collection<UUID> ids);

    @Query("""
            SELECT p FROM Product p
            WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :category, '%'))
            """)
    Page<Product> findByCategoryContaining(@Param("category") String category, Pageable pageable);
}
