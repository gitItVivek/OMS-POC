package com.inventoryservice.service;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockCommandDto;

public interface StockReservationService {

    void reserveStock(ReserveStockCommandDto command);

    void releaseStock(ReleaseStockCommandDto command);
}
