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
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = Topics.PRODUCT_COMMANDS_TOPIC)
public class ProductCommandsHandler {

    private final ProductService productService;
    private final OutboxEventService outboxEventService;
    private final ConsumedMessageService consumedMessageService;

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
        if (insufficientProductsList.isEmpty()) {
            // 2. Happy Path: All products are available
            Event availabilityConfirmedEvent = EventBuilder.availabilityConfirmedEvent(correlationId);
            this.outboxEventService.create(availabilityConfirmedEvent);
        } else {
            // 3. Failure Path: Stock shortage
            Event productShortageEvent = EventBuilder.productsShortageEvent(correlationId, insufficientProductsList);
            this.outboxEventService.create(productShortageEvent);
        }
    }

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
