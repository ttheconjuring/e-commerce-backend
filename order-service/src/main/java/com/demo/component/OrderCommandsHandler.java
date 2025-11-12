package com.demo.component;

import com.demo.common.command.order.CancelOrderCommand;
import com.demo.common.command.order.CompleteOrderCommand;
import com.demo.common.constant.Exceptions;
import com.demo.common.constant.Topics;
import com.demo.common.event.Event;
import com.demo.exception.CancelOrderNonRetryableException;
import com.demo.exception.CancelOrderRetryableException;
import com.demo.common.payload.order.CancelOrderPayload;
import com.demo.model.Status;
import com.demo.service.ConsumedMessageService;
import com.demo.service.OrderService;
import com.demo.service.OutboxEventService;
import com.demo.utility.EventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Listens for commands from the Saga Orchestrator on the {@link Topics#ORDER_COMMANDS_TOPIC}.
 * <p>
 * This handler is responsible for applying the *final* state to an order
 * based on the outcome of the saga. For example, it handles commands to
 * mark an order as {@link Status#COMPLETED} or {@link Status#CANCELLED}.
 * <p>
 * After processing a command, it publishes a corresponding event
 *  via the {@link OutboxEventService} to
 * notify the orchestrator that the action is done.
 * <p>
 * All handlers are idempotent (via {@link ConsumedMessageService}) and
 * transactional.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.ORDER_COMMANDS_TOPIC)
public class OrderCommandsHandler {

    private final OrderService orderService;
    private final OutboxEventService outboxEventService;
    private final ConsumedMessageService consumedMessageService;

    /**
     * Handles the {@link CompleteOrderCommand}, which is the terminal
     * command for a successful order saga.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Updates the order's status to {@link Status#COMPLETED}.</br>
     * 3. Creates an {@link EventBuilder#orderCompletedEvent(UUID)}
     * and saves it to the outbox to notify the orchestrator.
     *
     * @param completeOrderCommand The command from the saga orchestrator.
     */
    @Transactional
    @KafkaHandler
    public void handle(CompleteOrderCommand completeOrderCommand) {
        log.info("---> Received CompleteOrderCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(completeOrderCommand.getId())) return;

        UUID correlationId = completeOrderCommand.getCorrelationId();

        // 1. Apply final state to the Order
        this.orderService.updateStatus(correlationId, Status.COMPLETED);

        // 2. Publish "ack" event via outbox
        Event orderCompletedEvent = EventBuilder.orderCompletedEvent(correlationId);
        this.outboxEventService.create(orderCompletedEvent);
    }

    /**
     * Handles the {@link CancelOrderCommand}, which is the terminal
     * command for a failed or compensated order saga.
     * <p>
     * This method:</br>
     * 1. Checks for message duplication.</br>
     * 2. Updates the order's status to {@link Status#CANCELLED}.</br>
     * 3. Persists the cancellation reason from the command's payload.</br>
     * 4. Creates an {@link EventBuilder#orderCancelledEvent(UUID, String)}
     * and saves it to the outbox to notify the orchestrator.
     *
     * @param cancelOrderCommand The command from the saga, containing the reason for cancellation.
     */
    @Transactional
    @KafkaHandler
    public void handle(CancelOrderCommand cancelOrderCommand) {
        log.info("---> Received CancelOrderCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(cancelOrderCommand.getId())) return;

        // Uncomment to test error handling
        causeException(1); // 1 - retryable, 0 - non-retryable

        UUID correlationId = cancelOrderCommand.getCorrelationId();
        CancelOrderPayload cancelOrderPayload = (CancelOrderPayload) cancelOrderCommand.getPayload();
        String reason = cancelOrderPayload.getReason();

        // 1. Apply final state
        this.orderService.updateStatus(correlationId, Status.CANCELLED);

        // 2. Record the reason for cancellation
        this.orderService.setCancellationReason(correlationId, reason);

        // 3. Publish "ack" event via outbox
        Event orderCancelledEvent = EventBuilder.orderCancelledEvent(correlationId, reason);
        this.outboxEventService.create(orderCancelledEvent);
    }

    /**
     * A private utility method for testing the error handling and
     * Dead-Letter Topic (DLT) configuration.
     * <p>
     * This can be called manually from a handler to simulate either a
     * retryable or non-retryable processing failure.
     *
     * @param type 1 for a {@link CancelOrderRetryableException},
     * 0 for a {@link CancelOrderNonRetryableException}.
     */
    private void causeException(Integer type) {
        // Canceling Order Failure
        switch (type) {
            case 1 -> {
                log.error("<<< Retryable Exception >>> CancelOrderCommand");
                throw new CancelOrderRetryableException(String.format(Exceptions.RETRYABLE_EXCEPTION, Topics.ORDER_COMMANDS_TOPIC_DLT));
            }
            case 0 -> {
                log.error("<<< Non-Retryable Exception >>> CancelOrderCommand");
                throw new CancelOrderNonRetryableException(String.format(Exceptions.NONRETRYABLE_EXCEPTION, Topics.ORDER_COMMANDS_TOPIC_DLT));
            }
        }
    }

}
