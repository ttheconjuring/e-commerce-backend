package com.demo.component;

import com.demo.common.command.payment.ProcessPaymentCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.Event;
import com.demo.model.Payment;
import com.demo.model.Status;
import com.demo.service.ConsumedMessageService;
import com.demo.service.OutboxEventService;
import com.demo.service.PaymentService;
import com.demo.utility.EventBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(topics = Topics.PAYMENT_COMMANDS_TOPIC)
public class PaymentCommandsHandler {

    private final PaymentService paymentService;
    private final OutboxEventService outboxEventService;
    private final ConsumedMessageService consumedMessageService;

    @Transactional
    @KafkaHandler
    public void handle(ProcessPaymentCommand processPaymentCommand) {
        log.info("---> Received ProcessPaymentCommand <---");
        // Idempotency check
        if (this.consumedMessageService.isDuplicate(processPaymentCommand.getId())) return;
        // TODO: implement legit payment solution
        UUID correlationId = processPaymentCommand.getCorrelationId();
        // This 'if' block simulates the response from a real payment gateway
        if (true) {
            // Happy Path
            Payment payment = this.paymentService.save(processPaymentCommand.getPayload(), Status.PAYMENT_SUCCEEDED);
            // Create the success event
            Event paymentSucceededEvent = EventBuilder.paymentSucceededEvent(correlationId, payment.getTransactionId());
            this.outboxEventService.create(paymentSucceededEvent);
        } else {
            // Payment Failure
            Payment payment = this.paymentService.save(processPaymentCommand.getPayload(), Status.PAYMENT_FAILED);
            // Create the failure event
            Event paymentFailedEvent = EventBuilder.paymentFailedEvent(correlationId, payment.getFailureReason());
            this.outboxEventService.create(paymentFailedEvent);
        }
    }

}