package com.inventoryservice.service;

import com.inventoryservice.dto.ProductPageResponseDto;
import com.inventoryservice.dto.ProductSearchResultDto;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductPageResponseDto searchProducts(String query, int page, int size);

    List<ProductSearchResultDto> getProductsByIds(List<UUID> productIds);

    ProductSearchResultDto getProductById(UUID productId);

    ProductPageResponseDto getProductsByCategory(String category, int page, int size);
}
