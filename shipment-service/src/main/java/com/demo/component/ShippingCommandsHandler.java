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

/**
 * Listens for commands from the Saga Orchestrator on the {@link Topics#SHIPMENT_COMMANDS_TOPIC}.
 * <p>
 * This is the primary "ear" of the Shipping Service. It is responsible for
 * the core business logic of this service:
 * <ol>
 * <li><b>Arranging a shipment:</b> The "do" action for the saga.</li>
 * <li><b>Cancelling a shipment:</b> The "compensating" action (rollback)
 * if the saga fails later (e.g., payment failure).</li>
 * </ol>
 * It publishes the results of these actions back to the orchestrator
 * via the {@link OutboxEventService}.
 * <p>
 * All handlers are idempotent (via {@link ConsumedMessageService}) and
 * transactional.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.SHIPMENT_COMMANDS_TOPIC)
public class ShippingCommandsHandler {

    private final ShipmentService shipmentService;
    private final OutboxEventService outboxEventService;
    private final ConsumedMessageService consumedMessageService;

    /**
     * Handles the {@link ArrangeShipmentCommand} to book a shipment.
     * <p>
     * This method:
     * <ol>
     * <li>Checks for message duplication.</li>
     * <li><b>(TODO)</b> Simulates a shipment arrangement. A real implementation
     * would call an external carrier API (e.g., FedEx, UPS).</li>
     * <li><b>Happy Path:</b> If successful, saves a {@link Shipment} with
     * status {@link Status#ARRANGED} and creates a
     * {@code ShipmentArrangedEvent}.</li>
     * <li><b>Failure Path:</b> If failed, saves a {@link Shipment} with
     * status {@link Status#FAILED} and creates an
     * {@code ArrangementFailedEvent}.</li>
     * <li>Publishes the resulting event via the outbox in the same transaction.</li>
     * </ol>
     *
     * @param arrangeShipmentCommand The command containing customer and product details.
     */
    @Transactional
    @KafkaHandler
    public void handle(ArrangeShipmentCommand arrangeShipmentCommand) {
        log.info("---> Received ArrangeShipmentCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(arrangeShipmentCommand.getId())) return;

        // TODO: implement legit shipment arranging solution
        UUID correlationId = arrangeShipmentCommand.getCorrelationId();
        ArrangeShipmentPayload arrangeShipmentPayload = (ArrangeShipmentPayload) arrangeShipmentCommand.getPayload();

        // This 'if' block simulates the response from a real shipping/carrier API
        if (true) {
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

    /**
     * Handles the {@link CancelShipmentCommand}, which is a compensating transaction.
     * <p>
     * This command is typically received if the saga fails at a later
     * step, such as a payment failure.
     * <p>
     * This method:
     * <ol>
     * <li>Checks for message duplication.</li>
     * <li>Calls the {@link ShipmentService} to update the shipment's
     * status to {@link Status#CANCELLED} and record the reason.</li>
     * <li>Publishes a {@code ShipmentCancelledEvent} via the outbox as an
     * acknowledgment to the saga orchestrator that the rollback is complete.</li>
     * </ol>
     *
     * @param cancelShipmentCommand The command containing the ID of the
     * shipment to cancel and the reason.
     */
    @Transactional
    @KafkaHandler
    public void handle(CancelShipmentCommand cancelShipmentCommand) {
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
