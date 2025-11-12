package com.demo.component.impl;

import com.demo.common.command.payment.ProcessPaymentCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.payment.PaymentFailedEvent;
import com.demo.common.event.payment.PaymentSucceededEvent;
import com.demo.component.PaymentDltHandler;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete implementation of the {@link PaymentDltHandler} interface.
 * <p>
 * This class is a "multi-method" Kafka listener that consumes from
 * both the {@link Topics#PAYMENT_EVENTS_TOPIC_DLT} and
 * {@link Topics#PAYMENT_COMMANDS_TOPIC_DLT}.
 * <p>
 * The {@link KafkaHandler} annotation routes the deserialized message
 * to the correct method based on its class type (e.g., a
 * {@code PaymentFailedEvent} message goes to the
 * {@code handlePaymentFailedEvent} method).
 * <p>
 * The sole responsibility of each handler is to delegate the failed
 * message to the {@link DltMessageService} to be saved to the database.
 */
@Component
@RequiredArgsConstructor
@KafkaListener(topics = {Topics.PAYMENT_EVENTS_TOPIC_DLT, Topics.PAYMENT_COMMANDS_TOPIC_DLT})
public class PaymentDltHandlerImpl implements PaymentDltHandler {

    private final DltMessageService dltMessageService;

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handlePaymentFailedEvent(PaymentFailedEvent paymentFailedEvent) {
        this.dltMessageService.register(paymentFailedEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handlePaymentSucceededEvent(PaymentSucceededEvent paymentSucceededEvent) {
       this.dltMessageService.register(paymentSucceededEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This handler is transactional and registers the failed event.
     */
    @Transactional
    @KafkaHandler
    @Override
    public void handleProcessPaymentCommand(ProcessPaymentCommand processPaymentCommand) {
        this.dltMessageService.register(processPaymentCommand);
    }

}
