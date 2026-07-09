package com.inventoryservice.repository;

import com.inventoryservice.entity.StockReservation;
import com.inventoryservice.enums.StockReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StockReservationRepository extends JpaRepository<StockReservation, UUID> {

    List<StockReservation> findByOrderIdAndStatus(UUID orderId, StockReservationStatus status);

    boolean existsByOrderIdAndStatus(UUID orderId, StockReservationStatus status);
}
