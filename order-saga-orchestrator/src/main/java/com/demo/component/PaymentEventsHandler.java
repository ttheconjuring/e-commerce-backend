package com.demo.component;

import com.demo.common.command.Command;
import com.demo.common.constant.Topics;
import com.demo.common.event.payment.PaymentFailedEvent;
import com.demo.common.event.payment.PaymentSucceededEvent;
import com.demo.common.payload.payment.PaymentFailedPayload;
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
@KafkaListener(topics = Topics.PAYMENT_EVENTS_TOPIC)
public class PaymentEventsHandler {

    private final OrderStateService orderStateService;
    private final OutboxCommandService outboxCommandService;
    private final ConsumedMessageService consumedMessageService;

    @Transactional
    @KafkaHandler
    public void handle(PaymentSucceededEvent paymentSucceededEvent) {
        log.info("---> Received PaymentSucceededEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(paymentSucceededEvent.getId())) return;

        UUID correlationId = paymentSucceededEvent.getCorrelationId();

        // Persist the payment details to the order state
        this.orderStateService.reflectPayment(correlationId, paymentSucceededEvent.getPayload());
        OrderState orderState = this.orderStateService.retrieve(correlationId);

        // Create command for the next step (e.g., tell Product service to decrement stock)
        Command updateProductsCommand = CommandBuilder.updateProductsCommand(correlationId, orderState);
        this.outboxCommandService.create(updateProductsCommand);
    }

    @Transactional
    @KafkaHandler
    public void handle(PaymentFailedEvent paymentFailedEvent) {
        log.info("---> Received PaymentFailedEvent <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(paymentFailedEvent.getId())) return;

        UUID correlationId = paymentFailedEvent.getCorrelationId();
        PaymentFailedPayload paymentFailedPayload = (PaymentFailedPayload) paymentFailedEvent.getPayload();
        String reason = paymentFailedPayload.getReason();

        // Persist the failure details to the order state
        this.orderStateService.reflectPayment(correlationId, paymentFailedEvent.getPayload());

        // Update status to begin compensation
        OrderState orderState = this.orderStateService.updateStatus(correlationId, Status.PENDING_SHIPMENT_CANCELLATION);

        // Create compensating command to roll back the saga
        Command cancelOrderCommand = CommandBuilder.cancelShipmentCommand(correlationId, orderState, reason);
        this.outboxCommandService.create(cancelOrderCommand);
    }

}
