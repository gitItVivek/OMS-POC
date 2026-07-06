package com.inventoryservice.service.impl;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockItemDto;
import com.inventoryservice.entity.Product;
import com.inventoryservice.entity.StockReservation;
import com.inventoryservice.enums.StockReservationStatus;
import com.inventoryservice.exception.ProductNotFoundException;
import com.inventoryservice.repository.ProductRepository;
import com.inventoryservice.repository.StockReservationRepository;
import com.inventoryservice.service.InventoryBenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryBenchServiceImpl implements InventoryBenchService {

    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;

    @Override
    @Transactional
    public void reserveLine(UUID orderId, ReserveStockItemDto item) {
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

        if (product.getAvailableQty() < item.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for product " + item.getProductId());
        }

        product.setAvailableQty(product.getAvailableQty() - item.getQuantity());
        productRepository.save(product);

        stockReservationRepository.save(StockReservation.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .product(product)
                .quantity(item.getQuantity())
                .status(StockReservationStatus.RESERVED)
                .build());
    }

    @Override
    @Transactional
    public void releaseOrder(ReleaseStockCommandDto command) {
        stockReservationRepository.findByOrderIdAndStatus(command.getOrderId(), StockReservationStatus.RESERVED)
                .forEach(reservation -> {
                    Product product = reservation.getProduct();
                    product.setAvailableQty(product.getAvailableQty() + reservation.getQuantity());
                    productRepository.save(product);
                    reservation.setStatus(StockReservationStatus.RELEASED);
                    stockReservationRepository.save(reservation);
                });
    }
}
