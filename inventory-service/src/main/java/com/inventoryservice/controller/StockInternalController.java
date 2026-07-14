package com.inventoryservice.controller;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockCommandDto;
import com.inventoryservice.dto.StockReservationResultDto;
import com.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/stock")
@RequiredArgsConstructor
public class StockInternalController {

    private final StockReservationService stockReservationService;

    @PostMapping("/reserve")
    public StockReservationResultDto reserve(@RequestBody ReserveStockCommandDto command) {
        return stockReservationService.reserveStock(command);
    }

    @PostMapping("/release")
    public void release(@RequestBody ReleaseStockCommandDto command) {
        stockReservationService.releaseStock(command);
    }
}
