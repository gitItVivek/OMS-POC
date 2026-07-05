package com.inventoryservice.dal.impl;

import com.inventoryservice.dal.ProductDal;
import com.inventoryservice.entity.Product;
import com.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductDalImpl implements ProductDal {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.search(query.trim(), pageable);
    }

    @Override
    public List<Product> findProductsByIds(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return productRepository.findByIdIn(productIds);
    }

    @Override
    public Page<Product> findProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategoryContaining(category.trim(), pageable);
    }
}
