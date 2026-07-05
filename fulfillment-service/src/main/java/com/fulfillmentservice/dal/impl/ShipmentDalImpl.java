package com.fulfillmentservice.dal.impl;

import com.fulfillmentservice.dal.ShipmentDal;
import com.fulfillmentservice.entity.Shipment;
import com.fulfillmentservice.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ShipmentDalImpl implements ShipmentDal {

    private final ShipmentRepository shipmentRepository;

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return shipmentRepository.findByOrderId(orderId);
    }

    @Override
    public Shipment save(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }
}
