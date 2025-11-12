package com.demo.utility;

import com.demo.common.Type;
import com.demo.common.constant.Events;
import com.demo.common.event.Event;
import com.demo.common.event.shipment.ArrangementFailedEvent;
import com.demo.common.event.shipment.ShipmentArrangedEvent;
import com.demo.common.event.shipment.ShipmentCancelledEvent;
import com.demo.common.payload.shipment.ArrangementFailedPayload;
import com.demo.common.payload.shipment.ShipmentArrangedPayload;
import com.demo.common.payload.shipment.ShipmentCancelledPayload;
import com.demo.model.Shipment;

import java.time.Instant;
import java.util.UUID;

public class EventBuilder {

    private EventBuilder(){}

    public static Event shipmentArrangedEvent(UUID correlationId, Shipment shipment) {
        Event shipmentArrangedEvent = new ShipmentArrangedEvent();
        shipmentArrangedEvent.setId(UUID.randomUUID());
        shipmentArrangedEvent.setType(Type.EVENT);
        shipmentArrangedEvent.setName(Events.SHIPMENT_ARRANGED);
        shipmentArrangedEvent.setTimestamp(Instant.now());
        shipmentArrangedEvent.setCorrelationId(correlationId);
        ShipmentArrangedPayload shipmentArrangedPayload = new ShipmentArrangedPayload();
        shipmentArrangedPayload.setOrderId(correlationId);
        shipmentArrangedPayload.setShipmentId(shipment.getId());
        shipmentArrangedPayload.setTrackingNumber(shipment.getTrackingNumber());
        shipmentArrangedPayload.setCarrier(shipment.getCarrier().name());
        shipmentArrangedEvent.setPayload(shipmentArrangedPayload);
        return shipmentArrangedEvent;
    }

    public static Event shipmentArrangementFailedEvent(UUID correlationId, String failureReason) {
        Event arrangementFailedEvent = new ArrangementFailedEvent();
        arrangementFailedEvent.setId(UUID.randomUUID());
        arrangementFailedEvent.setType(Type.EVENT);
        arrangementFailedEvent.setName(Events.ARRANGEMENT_FAILED);
        arrangementFailedEvent.setTimestamp(Instant.now());
        arrangementFailedEvent.setCorrelationId(correlationId);
        ArrangementFailedPayload arrangementFailedPayload = new ArrangementFailedPayload();
        arrangementFailedPayload.setOrderId(correlationId);
        arrangementFailedPayload.setReason(failureReason);
        arrangementFailedPayload.setFailedAt(Instant.now());
        arrangementFailedEvent.setPayload(arrangementFailedPayload);
        return arrangementFailedEvent;
    }

    public static Event shipmentCancelledEvent(UUID correlationId, UUID shipmentId) {
        Event shipmentCancelledEvent = new ShipmentCancelledEvent();
        shipmentCancelledEvent.setId(UUID.randomUUID());
        shipmentCancelledEvent.setType(Type.EVENT);
        shipmentCancelledEvent.setName(Events.SHIPMENT_CANCELLED);
        shipmentCancelledEvent.setTimestamp(Instant.now());
        shipmentCancelledEvent.setCorrelationId(correlationId);
        ShipmentCancelledPayload shipmentCancelledPayload = new ShipmentCancelledPayload();
        shipmentCancelledPayload.setOrderId(correlationId);
        shipmentCancelledPayload.setShipmentId(shipmentId);
        shipmentCancelledEvent.setPayload(shipmentCancelledPayload);
        return shipmentCancelledEvent;
    }

}
