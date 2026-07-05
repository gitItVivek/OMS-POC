package com.fulfillmentservice.dal;

import com.fulfillmentservice.entity.Shipment;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentDal {

    Optional<Shipment> findByOrderId(UUID orderId);

    Shipment save(Shipment shipment);
}
