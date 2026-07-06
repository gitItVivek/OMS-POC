package com.fulfillmentservice.service.impl;

import com.fulfillmentservice.config.FulfillmentProperties;
import com.fulfillmentservice.dal.ShipmentDal;
import com.fulfillmentservice.dto.StartFulfillmentCommandDto;
import com.fulfillmentservice.entity.Shipment;
import com.fulfillmentservice.enums.ShipmentStatus;
import com.fulfillmentservice.service.FulfillmentBenchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class FulfillmentBenchServiceImpl implements FulfillmentBenchService {

    private final ShipmentDal shipmentDal;
    private final FulfillmentProperties fulfillmentProperties;

    @Override
    @Transactional
    public String startFulfillment(StartFulfillmentCommandDto command) {
        Shipment shipment = shipmentDal.findByOrderId(command.getOrderId())
                .orElseGet(() -> shipmentDal.save(Shipment.builder()
                        .id(UUID.randomUUID())
                        .orderId(command.getOrderId())
                        .status(ShipmentStatus.SHIPPED)
                        .carrier(fulfillmentProperties.getDefaultCarrier())
                        .trackingNumber(generateTrackingNumber())
                        .build()));
        return shipment.getTrackingNumber();
    }

    private String generateTrackingNumber() {
        int trackingNumber = ThreadLocalRandom.current().nextInt(
                fulfillmentProperties.getTrackingNumberMin(),
                fulfillmentProperties.getTrackingNumberMax());
        return fulfillmentProperties.getTrackingNumberPrefix() + trackingNumber;
    }
}
