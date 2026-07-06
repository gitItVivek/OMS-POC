package com.inventoryservice.service.impl;

import com.inventoryservice.dal.ProductDal;
import com.inventoryservice.dto.ProductPageResponseDto;
import com.inventoryservice.dto.ProductSearchResultDto;
import com.inventoryservice.entity.Product;
import com.inventoryservice.mapper.ProductMapper;
import com.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductDal productDal;
    private final ProductMapper productMapper;

    @Override
    public ProductPageResponseDto searchProducts(String query, int page, int size) {
        Page<Product> resultPage = productDal.searchProducts(query, PageRequest.of(page, size));
        return toPageResponse(resultPage);
    }

    @Override
    public List<ProductSearchResultDto> getProductsByIds(List<UUID> productIds) {
        return productMapper.toSearchResultDtos(productDal.findProductsByIds(productIds));
    }

    @Override
    public ProductSearchResultDto getProductById(UUID productId) {
        return productMapper.toSearchResultDto(productDal.findProductById(productId));
    }

    @Override
    public ProductPageResponseDto getProductsByCategory(String category, int page, int size) {
        Page<Product> resultPage = productDal.findProductsByCategory(category, PageRequest.of(page, size));
        return toPageResponse(resultPage);
    }

    private ProductPageResponseDto toPageResponse(Page<Product> resultPage) {
        return ProductPageResponseDto.builder()
                .items(productMapper.toSearchResultDtos(resultPage.getContent()))
                .page(resultPage.getNumber())
                .size(resultPage.getSize())
                .totalElements(resultPage.getTotalElements())
                .totalPages(resultPage.getTotalPages())
                .build();
    }
}
