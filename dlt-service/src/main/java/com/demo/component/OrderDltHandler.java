package com.demo.component;

import com.demo.common.command.order.CancelOrderCommand;
import com.demo.common.command.order.CompleteOrderCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.order.OrderCancelledEvent;
import com.demo.common.event.order.OrderCompletedEvent;
import com.demo.common.event.order.OrderCreatedEvent;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = {Topics.ORDER_EVENTS_TOPIC_DLT, Topics.ORDER_COMMANDS_TOPIC_DLT})
public class OrderDltHandler {

    private final DltMessageService dltMessageService;

    @Transactional
    @KafkaHandler
    public void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {
        this.dltMessageService.register(orderCreatedEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleOrderCompletedEvent(OrderCompletedEvent orderCompletedEvent) {
       this.dltMessageService.register(orderCompletedEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleOrderCancelledEvent(OrderCancelledEvent orderCancelledEvent) {
       this.dltMessageService.register(orderCancelledEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleCancelOrderCommand(CancelOrderCommand cancelOrderCommand) {
        this.dltMessageService.register(cancelOrderCommand);
    }

    @Transactional
    @KafkaHandler
    public void handleCompleteOrderCommand(CompleteOrderCommand completeOrderCommand) {
        this.dltMessageService.register(completeOrderCommand);
    }

}
