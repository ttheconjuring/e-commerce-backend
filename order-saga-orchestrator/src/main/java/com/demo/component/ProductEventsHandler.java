package com.demo.component;

import com.demo.common.command.Command;
import com.demo.common.constant.Topics;
import com.demo.common.event.product.AvailabilityConfirmedEvent;
import com.demo.common.event.product.ProductsShortageEvent;
import com.demo.common.event.product.ProductsUpdatedEvent;
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
 * Listens for events from the Product Service.
 * <p>
 * This component handles critical inventory-related events. It processes
 * confirmations of product availability, stock update completions, or
 * notifications of product shortages. Based on these events, it advances
 * the saga to the next step  or triggers a compensating rollback.
 * <p>
 * All event handling is idempotent, ensured by the {@link ConsumedMessageService}.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.PRODUCT_EVENTS_TOPIC)
public class ProductEventsHandler {

    private final OrderStateService orderStateService;
    private final OutboxCommandService outboxCommandService;
    private final ConsumedMessageService consumedMessageService;

    /**
     * Handles the {@link AvailabilityConfirmedEvent} (Happy Path).
     * <p>
     * This is triggered when the Product service confirms that all items
     * for the order are available and have been reserved.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Updates the saga status to {@link Status#PENDING_SHIPMENT_ARRANGEMENT}.</br>
     * 3. Creates an outbox command to trigger the Shipping Service.
     *
     * @param availabilityConfirmedEvent The event confirming product availability.
     */
    @Transactional
    @KafkaHandler
    public void handle(AvailabilityConfirmedEvent availabilityConfirmedEvent) {
        log.info("---> Received AvailabilityConfirmedEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(availabilityConfirmedEvent.getId())) return;

        UUID correlationId = availabilityConfirmedEvent.getCorrelationId();
        // Note: The first update to AVAILABILITY_CONFIRMED is immediately overwritten.
        this.orderStateService.updateStatus(correlationId, Status.AVAILABILITY_CONFIRMED);

        // Update state and get the latest version
        OrderState orderState = this.orderStateService.updateStatus(correlationId, Status.PENDING_SHIPMENT_ARRANGEMENT);

        // Create command for the next step (Shipping)
        Command arrangeShipmentCommand = CommandBuilder.arrangeShipmentCommand(correlationId, orderState);
        this.outboxCommandService.create(arrangeShipmentCommand);
    }

    /**
     * Handles the {@link ProductsUpdatedEvent}.
     * <p>
     * This event signifies that the product inventory has been
     * successfully decremented.<p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Updates the saga status to {@link Status#PENDING_COMPLETION}.</br>
     * 3. Creates a final outbox command to notify the Order service to
     * mark the order as {@link Status#COMPLETED}.
     *
     * @param productsUpdatedEvent The event confirming stock has been updated.
     */
    @Transactional
    @KafkaHandler
    public void handle(ProductsUpdatedEvent productsUpdatedEvent) {
        log.info("---> Received ProductsUpdatedEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(productsUpdatedEvent.getId())) return;

        UUID correlationId = productsUpdatedEvent.getCorrelationId();
        this.orderStateService.updateStatus(correlationId, Status.PENDING_COMPLETION);

        // Create command to finalize the order
        Command completeOrderCommand = CommandBuilder.completeOrderCommand(correlationId);
        this.outboxCommandService.create(completeOrderCommand);
    }

    /**
     * Handles the {@link ProductsShortageEvent} (Failure/Compensation Path).
     * <p>
     * This is triggered when the Product service reports that one or more
     * items are unavailable or out of stock.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Records the failure details (payload) using {@link OrderStateService#reflectProductsUnavailability}.</br>
     * 3. Updates the saga status to {@link Status#PENDING_CANCELLATION} to initiate a rollback.</br>
     * 4. Creates a compensating outbox command to cancel the order.
     *
     * @param productsShortageEvent The event detailing the item shortage.
     */
    @Transactional
    @KafkaHandler
    public void handle(ProductsShortageEvent productsShortageEvent) {
        log.info("---> Received ProductsShortageEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(productsShortageEvent.getId())) return;

        UUID correlationId = productsShortageEvent.getCorrelationId();

        // Record the failure reason and payload
        OrderState orderState = this.orderStateService.reflectProductsUnavailability(correlationId, productsShortageEvent.getPayload());

        // Set state to begin compensating
        this.orderStateService.updateStatus(correlationId, Status.PENDING_CANCELLATION);

        // Create compensating command
        Command cancelOrderCommand = CommandBuilder.cancelOrderCommand(correlationId, orderState.getFailureReason());
        this.outboxCommandService.create(cancelOrderCommand);
    }

}
