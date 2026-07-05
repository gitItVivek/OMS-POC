package com.orderservice.service.impl;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dal.SearchInterestDal;
import com.orderservice.dto.ProductPageDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.dto.SearchResponseDto;
import com.orderservice.entity.SearchInterest;
import com.orderservice.enums.TrendWindow;
import com.orderservice.service.SearchService;
import com.orderservice.util.TrendWindowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final int MAX_INTERESTS_PER_SEARCH = 10;

    private final InventoryServiceClient inventoryServiceClient;
    private final SearchInterestDal searchInterestDal;

    @Override
    @Transactional
    public SearchResponseDto search(UUID customerId, String query, int page, int size) {
        ProductPageDto results = inventoryServiceClient.searchProducts(query, page, size);
        int interestsRegistered = registerPassiveInterests(customerId, query, results.getItems());

        return SearchResponseDto.builder()
                .results(results)
                .interestsRegistered(interestsRegistered)
                .build();
    }

    private int registerPassiveInterests(UUID customerId, String query, List<ProductSummaryDto> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        List<SearchInterest> interests = new ArrayList<>();

        products.stream()
                .limit(MAX_INTERESTS_PER_SEARCH)
                .forEach(product -> {
                    interests.add(SearchInterest.builder()
                            .id(UUID.randomUUID())
                            .customerId(customerId)
                            .searchQuery(query)
                            .productId(product.getProductId())
                            .category(product.getCategory())
                            .build());

                    for (TrendWindow window : TrendWindow.values()) {
                        searchInterestDal.incrementTrend(
                                product.getProductId(),
                                window,
                                TrendWindowUtils.windowStart(window, today));
                    }
                });

        searchInterestDal.saveAllInterests(interests);
        return interests.size();
    }
}
