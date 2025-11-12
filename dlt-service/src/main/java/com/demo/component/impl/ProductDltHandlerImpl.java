package com.demo.component.impl;

import com.demo.common.command.product.ConfirmAvailabilityCommand;
import com.demo.common.command.product.UpdateProductsCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.product.AvailabilityConfirmedEvent;
import com.demo.common.event.product.ProductsShortageEvent;
import com.demo.common.event.product.ProductsUpdatedEvent;
import com.demo.component.ProductDltHandler;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of the {@link ProductDltHandler} interface.
 * <p>
 * This class is a "multi-method" Kafka listener that consumes from
 * both the {@link Topics#PRODUCT_EVENTS_TOPIC_DLT} and
 * {@link Topics#PRODUCT_COMMANDS_TOPIC_DLT}.
 * <p>
 * The {@link KafkaHandler} annotation routes the deserialized message
 * to the correct method based on its class type (e.g., a
 * {@code ProductsShortageEvent} message goes to the
 * {@code handleProductsShortageEvent} method).
 * <p>
 * The sole responsibility of each handler is to delegate the failed
 * message to the {@link DltMessageService} to be saved to the database.
 */
@Component
@KafkaListener(topics = {Topics.PRODUCT_EVENTS_TOPIC_DLT, Topics.PRODUCT_COMMANDS_TOPIC_DLT})
@RequiredArgsConstructor
public class ProductDltHandlerImpl implements ProductDltHandler {

    private final DltMessageService dltMessageService;

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleAvailabilityConfirmedEvent(AvailabilityConfirmedEvent availabilityConfirmedEvent) {
        this.dltMessageService.register(availabilityConfirmedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleProductsShortageEvent(ProductsShortageEvent productsShortageEvent) {
        this.dltMessageService.register(productsShortageEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleProductsUpdatedEvent(ProductsUpdatedEvent productsUpdatedEvent) {
        this.dltMessageService.register(productsUpdatedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleConfirmAvailabilityCommand(ConfirmAvailabilityCommand confirmAvailabilityCommand) {
        this.dltMessageService.register(confirmAvailabilityCommand);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleUpdateProductsCommand(UpdateProductsCommand updateProductsCommand) {
        this.dltMessageService.register(updateProductsCommand);
    }

}
