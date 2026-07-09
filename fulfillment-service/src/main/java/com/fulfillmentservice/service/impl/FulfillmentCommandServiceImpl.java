package com.fulfillmentservice.service.impl;

import com.fulfillmentservice.config.FulfillmentProperties;
import com.fulfillmentservice.dal.ShipmentDal;
import com.fulfillmentservice.dto.FulfillmentItemDto;
import com.fulfillmentservice.dto.ShipmentUpdatedEventDto;
import com.fulfillmentservice.dto.StartFulfillmentCommandDto;
import com.fulfillmentservice.entity.Shipment;
import com.fulfillmentservice.enums.ShipmentStatus;
import com.fulfillmentservice.kafka.FulfillmentEventPublisher;
import com.fulfillmentservice.kafka.KafkaPipelineTopics;
import com.fulfillmentservice.service.FulfillmentCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentCommandServiceImpl implements FulfillmentCommandService {

    private final ShipmentDal shipmentDal;
    private final FulfillmentProperties fulfillmentProperties;
    private final FulfillmentEventPublisher fulfillmentEventPublisher;

    @Override
    @Transactional
    public void startFulfillment(StartFulfillmentCommandDto command) {
        startFulfillment(command, KafkaPipelineTopics.SAGA);
    }

    @Override
    @Transactional
    public void startFulfillment(StartFulfillmentCommandDto command, KafkaPipelineTopics topics) {
        Shipment shipment = shipmentDal.findByOrderId(command.getOrderId())
                .orElseGet(() -> createAndShip(command));

        publishShipmentUpdated(shipment, topics);
        log.info("Fulfillment started for order {} with {} items", command.getOrderId(),
                command.getItems() != null ? command.getItems().size() : 0);
    }

    private Shipment createAndShip(StartFulfillmentCommandDto command) {
        Shipment shipment = Shipment.builder()
                .id(UUID.randomUUID())
                .orderId(command.getOrderId())
                .status(ShipmentStatus.SHIPPED)
                .carrier(fulfillmentProperties.getDefaultCarrier())
                .trackingNumber(generateTrackingNumber())
                .build();
        return shipmentDal.save(shipment);
    }

    private void publishShipmentUpdated(Shipment shipment, KafkaPipelineTopics topics) {
        fulfillmentEventPublisher.publishShipmentUpdated(ShipmentUpdatedEventDto.builder()
                .orderId(shipment.getOrderId())
                .shipmentId(shipment.getId())
                .status(shipment.getStatus())
                .trackingNumber(shipment.getTrackingNumber())
                .build(), topics.fulfillmentShipmentUpdatedEvent());
    }

    private String generateTrackingNumber() {
        int trackingNumber = ThreadLocalRandom.current().nextInt(
                fulfillmentProperties.getTrackingNumberMin(),
                fulfillmentProperties.getTrackingNumberMax());
        return fulfillmentProperties.getTrackingNumberPrefix() + trackingNumber;
    }
}
