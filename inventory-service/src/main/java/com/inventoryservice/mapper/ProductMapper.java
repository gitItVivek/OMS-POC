package com.inventoryservice.mapper;

import com.inventoryservice.dto.ProductSearchResultDto;
import com.inventoryservice.entity.Product;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductMapper {

    public ProductSearchResultDto toSearchResultDto(Product product) {
        return ProductSearchResultDto.builder()
                .productId(product.getId())
                .title(product.getTitle())
                .brand(product.getBrand())
                .category(product.getCategory())
                .price(product.getPrice())
                .currency(product.getCurrency())
                .availableQty(product.getAvailableQty())
                .build();
    }

    public List<ProductSearchResultDto> toSearchResultDtos(List<Product> products) {
        return products.stream().map(this::toSearchResultDto).toList();
    }
}
