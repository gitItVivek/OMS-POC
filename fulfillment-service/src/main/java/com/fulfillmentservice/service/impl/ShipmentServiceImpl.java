package com.fulfillmentservice.service.impl;

import com.fulfillmentservice.config.FulfillmentProperties;
import com.fulfillmentservice.dal.ShipmentDal;
import com.fulfillmentservice.dto.ShipmentResponseDto;
import com.fulfillmentservice.entity.Shipment;
import com.fulfillmentservice.enums.ShipmentStatus;
import com.fulfillmentservice.exception.InvalidShipmentStateException;
import com.fulfillmentservice.exception.ShipmentNotFoundException;
import com.fulfillmentservice.mapper.ShipmentMapper;
import com.fulfillmentservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentDal shipmentDal;
    private final ShipmentMapper shipmentMapper;
    private final FulfillmentProperties fulfillmentProperties;

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponseDto getByOrderId(UUID orderId) {
        Shipment shipment = shipmentDal.findByOrderId(orderId)
                .orElseThrow(() -> new ShipmentNotFoundException(orderId));
        return shipmentMapper.toResponseDto(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponseDto advance(UUID orderId) {
        Shipment shipment = shipmentDal.findByOrderId(orderId)
                .orElseGet(() -> createInitialShipment(orderId));

        switch (shipment.getStatus()) {
            case PACKED -> {
                shipment.setStatus(ShipmentStatus.SHIPPED);
                shipment.setTrackingNumber(generateTrackingNumber());
            }
            case SHIPPED -> shipment.setStatus(ShipmentStatus.DELIVERED);
            case DELIVERED -> throw new InvalidShipmentStateException("Shipment already delivered for order: " + orderId);
        }

        return shipmentMapper.toResponseDto(shipmentDal.save(shipment));
    }

    @Override
    @Transactional
    public ShipmentResponseDto fulfillToDelivered(UUID orderId) {
        Shipment shipment = shipmentDal.findByOrderId(orderId)
                .orElseGet(() -> createInitialShipment(orderId));

        if (shipment.getStatus() == ShipmentStatus.PACKED) {
            shipment.setStatus(ShipmentStatus.SHIPPED);
            shipment.setTrackingNumber(generateTrackingNumber());
            shipment = shipmentDal.save(shipment);
        }
        if (shipment.getStatus() == ShipmentStatus.SHIPPED) {
            shipment.setStatus(ShipmentStatus.DELIVERED);
            shipment = shipmentDal.save(shipment);
        }
        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            return shipmentMapper.toResponseDto(shipment);
        }
        throw new InvalidShipmentStateException("Cannot fulfill shipment for order: " + orderId);
    }

    private Shipment createInitialShipment(UUID orderId) {
        Shipment shipment = Shipment.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .status(ShipmentStatus.PACKED)
                .carrier(fulfillmentProperties.getDefaultCarrier())
                .build();
        return shipmentDal.save(shipment);
    }

    private String generateTrackingNumber() {
        int trackingNumber = ThreadLocalRandom.current().nextInt(
                fulfillmentProperties.getTrackingNumberMin(),
                fulfillmentProperties.getTrackingNumberMax());
        return fulfillmentProperties.getTrackingNumberPrefix() + trackingNumber;
    }
}
