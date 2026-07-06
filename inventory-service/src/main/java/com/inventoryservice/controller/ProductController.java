package com.inventoryservice.controller;

import com.inventoryservice.dto.ProductPageResponseDto;
import com.inventoryservice.dto.ProductSearchResultDto;
import com.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    public ProductPageResponseDto searchProducts(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return productService.searchProducts(query, page, size);
    }

    @GetMapping("/{id}")
    public ProductSearchResultDto getProductById(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping("/by-ids")
    public List<ProductSearchResultDto> getProductsByIds(@RequestParam("ids") List<UUID> ids) {
        return productService.getProductsByIds(ids);
    }

    @GetMapping("/by-category")
    public ProductPageResponseDto getProductsByCategory(
            @RequestParam("category") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getProductsByCategory(category, page, size);
    }
}
