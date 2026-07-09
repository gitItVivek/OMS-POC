package com.orderservice.event;

import com.orderservice.dto.SearchInterestRegisteredEventDto;
import com.orderservice.entity.SearchInterest;
import com.orderservice.kafka.SearchInterestEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SearchInterestEventListener {

    private final SearchInterestEventPublisher searchInterestEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSearchInterestsRegistered(SearchInterestsRegisteredEvent event) {
        for (SearchInterest interest : event.getInterests()) {
            searchInterestEventPublisher.publish(toDto(interest));
        }
    }

    private SearchInterestRegisteredEventDto toDto(SearchInterest interest) {
        return SearchInterestRegisteredEventDto.builder()
                .interestId(interest.getId())
                .customerId(interest.getCustomerId())
                .searchQuery(interest.getSearchQuery())
                .interestKey(interest.getInterestKey())
                .productId(interest.getProductId())
                .productTitle(interest.getProductTitle())
                .category(interest.getCategory())
                .registeredAt(interest.getCreatedAt())
                .build();
    }
}
