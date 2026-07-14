package com.inventoryservice.service;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockCommandDto;
import com.inventoryservice.dto.StockReservationResultDto;

public interface StockReservationService {

    StockReservationResultDto reserveStock(ReserveStockCommandDto command);

    void releaseStock(ReleaseStockCommandDto command);
}
