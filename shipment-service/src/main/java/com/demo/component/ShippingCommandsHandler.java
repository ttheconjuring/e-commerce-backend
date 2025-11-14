package com.demo.component;

import com.demo.common.command.shipment.ArrangeShipmentCommand;
import com.demo.common.command.shipment.CancelShipmentCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.Event;
import com.demo.common.payload.shipment.ArrangeShipmentPayload;
import com.demo.common.payload.shipment.CancelShipmentPayload;
import com.demo.model.Shipment;
import com.demo.model.Status;
import com.demo.service.ConsumedMessageService;
import com.demo.service.OutboxEventService;
import com.demo.service.ShipmentService;
import com.demo.utility.EventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.SHIPMENT_COMMANDS_TOPIC)
class ShippingCommandsHandler {

    private final ShipmentService shipmentService;
    private final OutboxEventService outboxEventService;
    private final ConsumedMessageService consumedMessageService;

    @Transactional
    @KafkaHandler
    void handle(ArrangeShipmentCommand arrangeShipmentCommand) {
        log.info("---> Received ArrangeShipmentCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(arrangeShipmentCommand.getId())) return;
        // TODO: implement legit shipment arranging solution
        UUID correlationId = arrangeShipmentCommand.getCorrelationId();
        ArrangeShipmentPayload arrangeShipmentPayload = (ArrangeShipmentPayload) arrangeShipmentCommand.getPayload();
        // This 'if' block simulates the response from a real shipping/carrier API
        if (false) {
            // Happy Path
            Shipment shipment = this.shipmentService.create(arrangeShipmentPayload, Status.ARRANGED);
            // Create the success event
            Event shipmentArrangedEvent = EventBuilder.shipmentArrangedEvent(correlationId, shipment);
            this.outboxEventService.create(shipmentArrangedEvent);
        } else {
            // Shipment Arrangement Failure
            Shipment shipment = this.shipmentService.create(arrangeShipmentPayload, Status.FAILED);
            // Create the failure event
            Event arrangementFailedEvent = EventBuilder.shipmentArrangementFailedEvent(correlationId, shipment.getFailureReason());
            this.outboxEventService.create(arrangementFailedEvent);
        }
    }

    @Transactional
    @KafkaHandler
    void handle(CancelShipmentCommand cancelShipmentCommand) {
        log.info("---> Received CancelShipmentCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(cancelShipmentCommand.getId())) return;
        UUID correlationId = cancelShipmentCommand.getCorrelationId();
        CancelShipmentPayload cancelShipmentPayload = (CancelShipmentPayload) cancelShipmentCommand.getPayload();
        UUID shipmentId = cancelShipmentPayload.getShipmentId();
        String cancellationReason = cancelShipmentPayload.getReason();
        // 1. Apply the cancellation
        this.shipmentService.cancel(shipmentId, cancellationReason);
        // 2. Publish "ack" event
        Event shipmentCancelledEvent = EventBuilder.shipmentCancelledEvent(correlationId, shipmentId);
        this.outboxEventService.create(shipmentCancelledEvent);
    }

}
