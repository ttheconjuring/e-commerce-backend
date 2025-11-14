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

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.SHIPMENT_EVENTS_TOPIC)
public class ShippingEventsHandler {

    private final OrderStateService orderStateService;
    private final OutboxCommandService outboxCommandService;
    private final ConsumedMessageService consumedMessageService;

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
