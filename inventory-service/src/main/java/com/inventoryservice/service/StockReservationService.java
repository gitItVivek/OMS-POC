package com.inventoryservice.service;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockCommandDto;
import com.inventoryservice.kafka.KafkaPipelineTopics;

public interface StockReservationService {

    void reserveStock(ReserveStockCommandDto command);

    void reserveStock(ReserveStockCommandDto command, KafkaPipelineTopics topics);

    void releaseStock(ReleaseStockCommandDto command);
}
