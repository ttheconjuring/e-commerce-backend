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

@Slf4j
@Component
@RequiredArgsConstructor
@KafkaListener(topics = Topics.ORDER_EVENTS_TOPIC)
public class OrderEventsHandler {

    private final OrderStateService orderStateService;
    private final OutboxCommandService outboxCommandService;
    private final ConsumedMessageService consumedMessageService;

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

    @Transactional
    @KafkaHandler
    public void handle(OrderCompletedEvent orderCompletedEvent) {
        log.info("---> Received OrderCompletedEvent <---");
        if (this.consumedMessageService.isDuplicate(orderCompletedEvent.getId())) return;
        UUID correlationId = orderCompletedEvent.getCorrelationId();
        this.orderStateService.updateStatus(correlationId, Status.COMPLETED);
    }

    @Transactional
    @KafkaHandler
    public void handle(OrderCancelledEvent orderCancelledEvent) {
        log.info("---> Received OrderCancelledEvent <---");
        if (this.consumedMessageService.isDuplicate(orderCancelledEvent.getId())) return;
        UUID correlationId = orderCancelledEvent.getCorrelationId();
        this.orderStateService.updateStatus(correlationId, Status.CANCELLED);
    }

}
