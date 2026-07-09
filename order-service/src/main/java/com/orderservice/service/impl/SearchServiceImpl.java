package com.orderservice.service.impl;

import com.orderservice.client.InventoryServiceClient;
import com.orderservice.dal.SearchInterestDal;
import com.orderservice.dto.ProductPageDto;
import com.orderservice.dto.ProductSummaryDto;
import com.orderservice.dto.SearchResponseDto;
import com.orderservice.entity.SearchInterest;
import com.orderservice.enums.TrendWindow;
import com.orderservice.config.OrderExperienceProperties;
import com.orderservice.event.SearchInterestsRegisteredEvent;
import com.orderservice.util.SearchInterestNormalizer;
import com.orderservice.web.RequestAuthContext;
import com.orderservice.service.SearchService;
import com.orderservice.util.TrendWindowUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final InventoryServiceClient inventoryServiceClient;
    private final SearchInterestDal searchInterestDal;
    private final OrderExperienceProperties orderExperienceProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public SearchResponseDto search(String query, int page, int size) {
        UUID userId = RequestAuthContext.currentUserId();
        String trimmedQuery = query == null ? "" : query.trim();
        ProductPageDto results = inventoryServiceClient.searchProducts(trimmedQuery, page, size);
        int interestsRegistered = registerPassiveInterests(userId, trimmedQuery, results.getItems());

        return SearchResponseDto.builder()
                .results(results)
                .interestsRegistered(interestsRegistered)
                .build();
    }

    private int registerPassiveInterests(UUID userId, String query, List<ProductSummaryDto> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        String interestKey = SearchInterestNormalizer.normalizeInterestKey(query);
        if (interestKey.isEmpty()) {
            return 0;
        }

        if (searchInterestDal.hasInterestForKey(userId, interestKey)) {
            recordTrendsOnly(products);
            return 0;
        }

        ProductSummaryDto topProduct = products.getFirst();
        String categoryLabel = SearchInterestNormalizer.extractCategoryLabel(topProduct.getCategory());

        SearchInterest interest = SearchInterest.builder()
                .id(UUID.randomUUID())
                .customerId(userId)
                .searchQuery(query)
                .interestKey(interestKey)
                .productId(topProduct.getProductId())
                .productTitle(topProduct.getTitle())
                .category(categoryLabel)
                .build();

        recordTrendsOnly(products);
        searchInterestDal.saveAllInterests(List.of(interest));
        applicationEventPublisher.publishEvent(new SearchInterestsRegisteredEvent(this, List.of(interest)));
        return 1;
    }

    private void recordTrendsOnly(List<ProductSummaryDto> products) {
        LocalDate today = LocalDate.now();
        products.stream()
                .limit(orderExperienceProperties.getSearch().getMaxInterestsPerSearch())
                .forEach(product -> {
                    for (TrendWindow window : TrendWindow.values()) {
                        searchInterestDal.incrementTrend(
                                product.getProductId(),
                                window,
                                TrendWindowUtils.windowStart(window, today));
                    }
                });
    }
}
