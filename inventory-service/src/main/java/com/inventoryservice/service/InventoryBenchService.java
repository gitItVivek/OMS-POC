package com.inventoryservice.service;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockItemDto;

import java.util.UUID;

public interface InventoryBenchService {

    void reserveLine(UUID orderId, ReserveStockItemDto item);

    void releaseOrder(ReleaseStockCommandDto command);
}
