package com.inventoryservice.service.impl;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockCommandDto;
import com.inventoryservice.dto.ReserveStockItemDto;
import com.inventoryservice.dto.StockReservationResultDto;
import com.inventoryservice.entity.Product;
import com.inventoryservice.entity.StockReservation;
import com.inventoryservice.enums.StockReservationStatus;
import com.inventoryservice.exception.ProductNotFoundException;
import com.inventoryservice.repository.ProductRepository;
import com.inventoryservice.repository.StockReservationRepository;
import com.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockReservationServiceImpl implements StockReservationService {

    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;

    @Override
    @Transactional
    public StockReservationResultDto reserveStock(ReserveStockCommandDto command) {
        if (stockReservationRepository.existsByOrderIdAndStatus(command.getOrderId(), StockReservationStatus.RESERVED)) {
            return StockReservationResultDto.builder()
                    .orderId(command.getOrderId())
                    .success(true)
                    .message("Stock already reserved")
                    .build();
        }

        for (ReserveStockItemDto item : command.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            if (product.getAvailableQty() < item.getQuantity()) {
                return StockReservationResultDto.builder()
                        .orderId(command.getOrderId())
                        .success(false)
                        .message("Insufficient stock for product " + item.getProductId())
                        .build();
            }
        }

        for (ReserveStockItemDto item : command.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            product.setAvailableQty(product.getAvailableQty() - item.getQuantity());
            productRepository.save(product);

            stockReservationRepository.save(StockReservation.builder()
                    .id(UUID.randomUUID())
                    .orderId(command.getOrderId())
                    .product(product)
                    .quantity(item.getQuantity())
                    .status(StockReservationStatus.RESERVED)
                    .build());
        }

        return StockReservationResultDto.builder()
                .orderId(command.getOrderId())
                .success(true)
                .message("Stock reserved")
                .build();
    }

    @Override
    @Transactional
    public void releaseStock(ReleaseStockCommandDto command) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderIdAndStatus(
                command.getOrderId(), StockReservationStatus.RESERVED);

        for (StockReservation reservation : reservations) {
            Product product = reservation.getProduct();
            product.setAvailableQty(product.getAvailableQty() + reservation.getQuantity());
            productRepository.save(product);

            reservation.setStatus(StockReservationStatus.RELEASED);
            stockReservationRepository.save(reservation);
        }
    }
}
