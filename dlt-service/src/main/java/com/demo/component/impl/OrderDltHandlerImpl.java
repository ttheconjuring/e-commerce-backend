package com.demo.component.impl;

import com.demo.common.command.order.CancelOrderCommand;
import com.demo.common.command.order.CompleteOrderCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.order.OrderCancelledEvent;
import com.demo.common.event.order.OrderCompletedEvent;
import com.demo.common.event.order.OrderCreatedEvent;
import com.demo.component.OrderDltHandler;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of the {@link OrderDltHandler} interface.
 * <p>
 * This class is a "multi-method" Kafka listener that consumes from
 * both the {@link Topics#ORDER_EVENTS_TOPIC_DLT} and
 * {@link Topics#ORDER_COMMANDS_TOPIC_DLT}.
 * <p>
 * Spring's {@link KafkaHandler} annotation automatically routes the
 * deserialized message to the correct method based on its class type
 * (e.g., an {@code OrderCreatedEvent} message goes to the
 * {@code handleOrderCreatedEvent} method).
 * <p>
 * The sole responsibility of each handler is to delegate the failed
 * message to the {@link DltMessageService} to be saved to the database
 * for administrative review.
 */
@Component
@KafkaListener(topics = {Topics.ORDER_EVENTS_TOPIC_DLT, Topics.ORDER_COMMANDS_TOPIC_DLT})
@RequiredArgsConstructor
public class OrderDltHandlerImpl implements OrderDltHandler {

    private final DltMessageService dltMessageService;

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {
        this.dltMessageService.register(orderCreatedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleOrderCompletedEvent(OrderCompletedEvent orderCompletedEvent) {
       this.dltMessageService.register(orderCompletedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleOrderCancelledEvent(OrderCancelledEvent orderCancelledEvent) {
       this.dltMessageService.register(orderCancelledEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleCancelOrderCommand(CancelOrderCommand cancelOrderCommand) {
        this.dltMessageService.register(cancelOrderCommand);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleCompleteOrderCommand(CompleteOrderCommand completeOrderCommand) {
        this.dltMessageService.register(completeOrderCommand);
    }

}
