package com.demo.component;

import com.demo.common.command.Command;
import com.demo.common.constant.Topics;
import com.demo.common.event.order.OrderCancelledEvent;
import com.demo.common.event.order.OrderCompletedEvent;
import com.demo.common.event.order.OrderCreatedEvent;
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
 * Main event listener for the Order Saga Orchestrator.
 * <p>
 * This component listens to the {@link Topics#ORDER_EVENTS_TOPIC} for various
 * order-related events. It acts as the primary "brain" of the saga, delegating
 * tasks to the {@link OrderStateService} to update the saga's state and
 * using the {@link OutboxCommandService} to dispatch new commands for
 * subsequent steps in the process.
 * <p>
 * All event handling is idempotent, ensured by the {@link ConsumedMessageService}.
 * Each handler runs within a database transaction to ensure atomicity.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = Topics.ORDER_EVENTS_TOPIC)
public class OrderEventsHandler {

    private final OrderStateService orderStateService;
    private final OutboxCommandService outboxCommandService;
    private final ConsumedMessageService consumedMessageService;

    /**
     * Handles the initial {@link OrderCreatedEvent}, which kicks off the saga.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Creates the initial {@link com.demo.model.OrderState}.</br>
     * 3. Updates the status to {@link Status#PENDING_AVAILABILITY_CONFIRMATION}.</br>
     * 4. Creates an outbox command to trigger the Product/Inventory service.
     *
     * @param orderCreatedEvent The event containing the new order details.
     */
    @Transactional
    @KafkaHandler
    public void handle(OrderCreatedEvent orderCreatedEvent) {
        log.info("---> Received OrderCreatedEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(orderCreatedEvent.getId())) return;

        // Create the saga state machine
        this.orderStateService.create(orderCreatedEvent);
        this.orderStateService.updateStatus(orderCreatedEvent.getCorrelationId(), Status.PENDING_AVAILABILITY_CONFIRMATION);

        // Dispatch command for the next step (e.g., to Product Service)
        Command confirmAvailabilityCommand = CommandBuilder.confirmAvailabilityCommand(orderCreatedEvent);
        this.outboxCommandService.create(confirmAvailabilityCommand);
    }

    /**
     * Handles the {@link OrderCompletedEvent}, a terminal event for a successful saga.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Updates the saga status to {@link Status#COMPLETED}.
     *
     * @param orderCompletedEvent The event signaling successful order completion.
     */
    @Transactional
    @KafkaHandler
    public void handle(OrderCompletedEvent orderCompletedEvent) {
        log.info("---> Received OrderCompletedEvent <---");
        if (this.consumedMessageService.isDuplicate(orderCompletedEvent.getId())) return;

        UUID correlationId = orderCompletedEvent.getCorrelationId();
        this.orderStateService.updateStatus(correlationId, Status.COMPLETED);
    }

    /**
     * Handles the {@link OrderCancelledEvent}, a terminal event for a failed or cancelled saga.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Updates the saga status to {@link Status#CANCELLED}.
     *
     * @param orderCancelledEvent The event signaling order cancellation.
     */
    @Transactional
    @KafkaHandler
    public void handle(OrderCancelledEvent orderCancelledEvent) {
        log.info("---> Received OrderCancelledEvent <---");
        if (this.consumedMessageService.isDuplicate(orderCancelledEvent.getId())) return;

        UUID correlationId = orderCancelledEvent.getCorrelationId();
        this.orderStateService.updateStatus(correlationId, Status.CANCELLED);
    }

}
