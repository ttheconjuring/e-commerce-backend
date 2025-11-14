package com.demo.component;

import com.demo.common.command.product.ConfirmAvailabilityCommand;
import com.demo.common.command.product.UpdateProductsCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.product.AvailabilityConfirmedEvent;
import com.demo.common.event.product.ProductsShortageEvent;
import com.demo.common.event.product.ProductsUpdatedEvent;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = {Topics.PRODUCT_EVENTS_TOPIC_DLT, Topics.PRODUCT_COMMANDS_TOPIC_DLT})
public class ProductDltHandler {

    private final DltMessageService dltMessageService;

    @Transactional
    @KafkaHandler
    public void handleAvailabilityConfirmedEvent(AvailabilityConfirmedEvent availabilityConfirmedEvent) {
        this.dltMessageService.register(availabilityConfirmedEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleProductsShortageEvent(ProductsShortageEvent productsShortageEvent) {
        this.dltMessageService.register(productsShortageEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleProductsUpdatedEvent(ProductsUpdatedEvent productsUpdatedEvent) {
        this.dltMessageService.register(productsUpdatedEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleConfirmAvailabilityCommand(ConfirmAvailabilityCommand confirmAvailabilityCommand) {
        this.dltMessageService.register(confirmAvailabilityCommand);
    }

    @Transactional
    @KafkaHandler
    public void handleUpdateProductsCommand(UpdateProductsCommand updateProductsCommand) {
        this.dltMessageService.register(updateProductsCommand);
    }

}
