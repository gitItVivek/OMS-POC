package com.orderservice.service;

import com.orderservice.dto.SearchResponseDto;

import java.util.UUID;

public interface SearchService {

    SearchResponseDto search(String query, int page, int size);
}
