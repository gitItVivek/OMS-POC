package com.inventoryservice.dal;

import com.inventoryservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductDal {

    Page<Product> searchProducts(String query, Pageable pageable);

    List<Product> findProductsByIds(List<UUID> productIds);

    Product findProductById(UUID productId);

    Page<Product> findProductsByCategory(String category, Pageable pageable);
}
