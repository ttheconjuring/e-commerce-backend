package com.demo.component;

import com.demo.common.command.Command;
import com.demo.common.constant.Topics;
import com.demo.common.event.shipment.ArrangementFailedEvent;
import com.demo.common.event.shipment.ShipmentArrangedEvent;
import com.demo.common.event.shipment.ShipmentCancelledEvent;
import com.demo.model.OrderState;
import com.demo.model.Status;
import com.demo.service.ConsumedMessageService;
import com.demo.service.OrderStateService;
import com.demo.service.OutboxCommandService;
import com.demo.utility.CommandBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Listens for events from the Shipping Service.
 * <p>
 * This component handles the outcome of the shipment arrangement step.
 * Based on this saga's design, a successful shipment arrangement
 * is the trigger to proceed with customer payment.
 * <p>
 * A failure at this stage (either arrangement failure or cancellation)
 * will trigger a compensating rollback for the entire saga.
 * <p>
 * All event handling is idempotent, ensured by the {@link ConsumedMessageService}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.SHIPMENT_EVENTS_TOPIC)
public class ShippingEventsHandler {

    private final OrderStateService orderStateService;
    private final OutboxCommandService outboxCommandService;
    private final ConsumedMessageService consumedMessageService;

    /**
     * Handles the {@link ShipmentArrangedEvent} (Happy Path).
     * <p>
     * This event signifies that the Shipping Service has successfully
     * reserved a courier and the order is ready to be paid for.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Records the shipment details in the {@link OrderState}.</br>
     * 3. Updates the saga status to {@link Status#PENDING_PAYMENT}.</br>
     * 4. Creates a new outbox command to trigger the Payment Service.
     *
     * @param shipmentArrangedEvent The event from the Shipping Service.
     */
    @Transactional
    @KafkaHandler
    public void handle(ShipmentArrangedEvent shipmentArrangedEvent) {
        log.info("---> Received ShipmentArrangedEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(shipmentArrangedEvent.getId())) return;

        UUID correlationId = shipmentArrangedEvent.getCorrelationId();

        // Record the shipment details
        this.orderStateService.reflectShipmentArrangement(correlationId, shipmentArrangedEvent.getPayload());

        // Update state and get the latest version
        OrderState orderState = this.orderStateService.updateStatus(correlationId, Status.PENDING_PAYMENT);

        // Create command for the next step (Payment)
        Command processPaymentCommand = CommandBuilder.processPaymentCommand(correlationId, orderState);
        this.outboxCommandService.create(processPaymentCommand);
    }

    /**
     * Handles the {@link ArrangementFailedEvent} (Failure Path).
     * <p>
     * Triggered if the Shipping Service fails to arrange a shipment.<p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Records the failure details in the {@link OrderState}.</br>
     * 3. Updates the saga status to {@link Status#PENDING_CANCELLATION} to initiate a rollback.</br>
     * 4. Creates a compensating outbox command to cancel the order.
     *
     * @param arrangementFailedEvent The event detailing the arrangement failure.
     */
    @Transactional
    @KafkaHandler
    public void handle(ArrangementFailedEvent arrangementFailedEvent) {
        log.info("---> Received ArrangementFailedEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(arrangementFailedEvent.getId())) return;

        UUID correlationId = arrangementFailedEvent.getCorrelationId();

        // Record the failure details
        this.orderStateService.reflectShipmentArrangementFailure(correlationId, arrangementFailedEvent.getPayload());

        // Set state to begin compensating
        OrderState orderState = this.orderStateService.updateStatus(correlationId, Status.PENDING_CANCELLATION);

        // Create compensating command
        Command cancelOrderCommand = CommandBuilder.cancelOrderCommand(correlationId, orderState.getFailureReason());
        this.outboxCommandService.create(cancelOrderCommand);
    }

    /**
     * Handles the {@link ShipmentCancelledEvent} (Compensation Path).
     * <p>
     * This event confirms that a previously arranged shipment has been successfully
     * cancelled. It is received as part of a larger compensating transaction.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Updates the saga status to {@link Status#PENDING_CANCELLATION}.</br>
     * 3. Creates a final outbox command to cancel the Order itself.
     *
     * @param shipmentCancelledEvent The event confirming the shipment rollback.
     */
    @Transactional
    @KafkaHandler
    public void handle(ShipmentCancelledEvent shipmentCancelledEvent) {
        log.info("---> Received ShipmentCancelledEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(shipmentCancelledEvent.getId())) return;

        UUID correlationId = shipmentCancelledEvent.getCorrelationId();

        // Update state and get reason (if any)
        OrderState orderState = this.orderStateService.updateStatus(correlationId, Status.PENDING_CANCELLATION);

        // Create the final compensating command to cancel the order
        Command cancelOrderCommand = CommandBuilder.cancelOrderCommand(correlationId, orderState.getFailureReason());
        this.outboxCommandService.create(cancelOrderCommand);
    }

}
