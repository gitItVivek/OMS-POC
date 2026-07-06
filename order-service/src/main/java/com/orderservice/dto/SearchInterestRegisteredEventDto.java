package com.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchInterestRegisteredEventDto {

    private UUID interestId;
    private UUID customerId;
    private String searchQuery;
    private String interestKey;
    private UUID productId;
    private String productTitle;
    private String category;
    private Instant registeredAt;
}
