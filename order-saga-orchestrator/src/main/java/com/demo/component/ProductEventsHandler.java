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

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.PRODUCT_EVENTS_TOPIC)
public class ProductEventsHandler {

    private final OrderStateService orderStateService;
    private final OutboxCommandService outboxCommandService;
    private final ConsumedMessageService consumedMessageService;

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
