package com.inventoryservice.controller;

import com.inventoryservice.dto.BenchReserveLineRequestDto;
import com.inventoryservice.dto.BenchStepResponseDto;
import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockItemDto;
import com.inventoryservice.service.InventoryBenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/bench")
@RequiredArgsConstructor
public class InventoryBenchController {

    private final InventoryBenchService inventoryBenchService;

    @PostMapping("/reservations/line")
    public BenchStepResponseDto reserveLine(@RequestBody BenchReserveLineRequestDto request) {
        inventoryBenchService.reserveLine(
                request.getOrderId(),
                ReserveStockItemDto.builder()
                        .productId(request.getProductId())
                        .quantity(request.getQuantity())
                        .build());
        return BenchStepResponseDto.builder()
                .success(true)
                .step("RESERVE_LINE")
                .message("Stock reserved for line item")
                .build();
    }

    @PostMapping("/reservations/{orderId}/release")
    public BenchStepResponseDto release(@PathVariable UUID orderId) {
        inventoryBenchService.releaseOrder(ReleaseStockCommandDto.builder().orderId(orderId).build());
        return BenchStepResponseDto.builder()
                .success(true)
                .step("RELEASE_STOCK")
                .message("Stock released")
                .build();
    }
}
