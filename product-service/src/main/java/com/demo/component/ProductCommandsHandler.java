package com.demo.component;

import com.demo.common.command.product.ConfirmAvailabilityCommand;
import com.demo.common.command.product.UpdateProductsCommand;
import com.demo.common.constant.Commands;
import com.demo.common.constant.Topics;
import com.demo.common.dto.InsufficientProductDTO;
import com.demo.common.dto.ProductQuantityDTO;
import com.demo.common.event.Event;
import com.demo.common.payload.product.*;
import com.demo.service.ConsumedMessageService;
import com.demo.service.OutboxEventService;
import com.demo.service.ProductService;
import com.demo.utility.EventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Listens for commands from the Saga Orchestrator on the {@link Topics#PRODUCT_COMMANDS_TOPIC}.
 * <p>
 * This is the primary "ear" of the Product Service. It is responsible for
 * all inventory-related logic, which includes:
 * <ol>
 * <li><b>Confirming Availability:</b> The "reservation" step of the saga.</li>
 * <li><b>Updating Products:</b> The "commit" step (decrementing stock) after
 * a successful payment.</li>
 * </ol>
 * It publishes the results of these actions (e.g., {@code AvailabilityConfirmedEvent},
 * {@code ProductsShortageEvent}) back to the orchestrator via the
 * {@link OutboxEventService}.
 * <p>
 * All handlers are idempotent (via {@link ConsumedMessageService}) and
 * transactional.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = Topics.PRODUCT_COMMANDS_TOPIC)
public class ProductCommandsHandler {

    private final ProductService productService;
    private final OutboxEventService outboxEventService;
    private final ConsumedMessageService consumedMessageService;

    /**
     * Handles the {@link ConfirmAvailabilityCommand} (the "reservation" step).
     * <p>
     * This method:
     * <ol>
     * <li>Checks for message duplication.</li>
     * <li>Delegates to the {@link ProductService} to check if the requested
     * product quantities are available.</li>
     * <li><b>Happy Path:</b> If stock is sufficient (service returns null),
     * it publishes an {@code AvailabilityConfirmedEvent}.</li>
     * <li><b>Failure Path:</b> If stock is insufficient (service returns a list
     * of unavailable products), it publishes a {@code ProductsShortageEvent}.</li>
     * </ol>
     * The resulting event is saved to the outbox in the same transaction.
     *
     * @param confirmAvailabilityCommand The command containing the list of products to check.
     */
    @Transactional
    @KafkaHandler
    public void handle(ConfirmAvailabilityCommand confirmAvailabilityCommand) {
        log.info("---> Received ConfirmAvailabilityCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(confirmAvailabilityCommand.getId())) return;

        UUID correlationId = confirmAvailabilityCommand.getCorrelationId();
        ConfirmAvailabilityPayload confirmAvailabilityPayload = (ConfirmAvailabilityPayload) confirmAvailabilityCommand.getPayload();
        List<ProductQuantityDTO> productsToCheck = confirmAvailabilityPayload.getProductsToCheck();

        // 1. Delegate to the service to check stock
        List<InsufficientProductDTO> insufficientProductsList = this.productService.confirmAvailability(productsToCheck);

        if (insufficientProductsList == null) {
            // 2. Happy Path: All products are available
            Event availabilityConfirmedEvent = EventBuilder.availabilityConfirmedEvent(correlationId);
            this.outboxEventService.create(availabilityConfirmedEvent);
        } else {
            // 3. Failure Path: Stock shortage
            Event productShortageEvent = EventBuilder.productsShortageEvent(correlationId, insufficientProductsList);
            this.outboxEventService.create(productShortageEvent);
        }
    }

    /**
     * Handles the {@link UpdateProductsCommand} (the "commit" step).
     * <p>
     * This command is received after a payment has been successfully
     * processed, indicating that the stock should be permanently decremented.
     * <p>
     * This method:
     * <ol>
     * <li>Checks for message duplication.</li>
     * <li>Delegates to the {@link ProductService} to decrement the stock quantities.</li>
     * <li>Publishes a {@code ProductsUpdatedEvent} via the outbox as an
     * acknowledgment to the saga orchestrator.</li>
     * </ol>
     *
     * @param updateProductsCommand The command containing the list of products to decrement.
     */
    @Transactional
    @KafkaHandler
    public void handle(UpdateProductsCommand updateProductsCommand) {
        log.info("---> Received UpdateProductsCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(updateProductsCommand.getId())) return;

        UUID correlationId = updateProductsCommand.getCorrelationId();
        UpdateProductsPayload updateProductsPayload = (UpdateProductsPayload) updateProductsCommand.getPayload();
        List<ProductQuantityDTO> productsToDecrement = updateProductsPayload.getProductsToDecrement();

        // 1. Delegate to the service to decrement stock
        this.productService.updateProductsQuantity(productsToDecrement, Commands.UPDATE_PRODUCTS);

        // 2. Publish "ack" event
        Event productsUpdatedEvent = EventBuilder.productsUpdatedEvent(correlationId);
        this.outboxEventService.create(productsUpdatedEvent);
    }

}
