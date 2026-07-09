package com.inventoryservice.service.impl;

import com.inventoryservice.dto.ReleaseStockCommandDto;
import com.inventoryservice.dto.ReserveStockCommandDto;
import com.inventoryservice.dto.ReserveStockItemDto;
import com.inventoryservice.dto.StockReservationFailedEventDto;
import com.inventoryservice.dto.StockReservedEventDto;
import com.inventoryservice.entity.Product;
import com.inventoryservice.entity.StockReservation;
import com.inventoryservice.enums.StockReservationStatus;
import com.inventoryservice.exception.ProductNotFoundException;
import com.inventoryservice.kafka.InventoryEventPublisher;
import com.inventoryservice.repository.ProductRepository;
import com.inventoryservice.repository.StockReservationRepository;
import com.inventoryservice.service.StockReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationServiceImpl implements StockReservationService {

    private final ProductRepository productRepository;
    private final StockReservationRepository stockReservationRepository;
    private final InventoryEventPublisher inventoryEventPublisher;

    @Override
    @Transactional
    public void reserveStock(ReserveStockCommandDto command) {
        if (stockReservationRepository.existsByOrderIdAndStatus(command.getOrderId(), StockReservationStatus.RESERVED)) {
            log.warn("Stock already reserved for order {} — publishing reserved event", command.getOrderId());
            publishReserved(command);
            return;
        }

        List<ReserveStockItemDto> reservedItems = new ArrayList<>();

        for (ReserveStockItemDto item : command.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            if (product.getAvailableQty() < item.getQuantity()) {
                publishFailed(command, "Insufficient stock for product " + item.getProductId());
                return;
            }

            product.setAvailableQty(product.getAvailableQty() - item.getQuantity());
            productRepository.save(product);

            stockReservationRepository.save(StockReservation.builder()
                    .id(UUID.randomUUID())
                    .orderId(command.getOrderId())
                    .product(product)
                    .quantity(item.getQuantity())
                    .status(StockReservationStatus.RESERVED)
                    .build());

            reservedItems.add(item);
        }

        publishReserved(ReserveStockCommandDto.builder()
                .orderId(command.getOrderId())
                .items(reservedItems)
                .build());
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

        log.info("Released {} stock reservations for order {}", reservations.size(), command.getOrderId());
    }

    private void publishReserved(ReserveStockCommandDto command) {
        inventoryEventPublisher.publishStockReserved(StockReservedEventDto.builder()
                .orderId(command.getOrderId())
                .items(command.getItems())
                .build());
    }

    private void publishFailed(ReserveStockCommandDto command, String reason) {
        inventoryEventPublisher.publishStockReservationFailed(StockReservationFailedEventDto.builder()
                .orderId(command.getOrderId())
                .items(command.getItems())
                .reason(reason)
                .build());
    }
}
