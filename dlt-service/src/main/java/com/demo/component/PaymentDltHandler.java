package com.demo.component;

import com.demo.common.command.payment.ProcessPaymentCommand;
import com.demo.common.constant.Topics;
import com.demo.common.event.payment.PaymentFailedEvent;
import com.demo.common.event.payment.PaymentSucceededEvent;
import com.demo.service.DltMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@KafkaListener(topics = {Topics.PAYMENT_EVENTS_TOPIC_DLT, Topics.PAYMENT_COMMANDS_TOPIC_DLT})
public class PaymentDltHandler {

    private final DltMessageService dltMessageService;

    @Transactional
    @KafkaHandler
    public void handlePaymentFailedEvent(PaymentFailedEvent paymentFailedEvent) {
        this.dltMessageService.register(paymentFailedEvent);
    }


    @Transactional
    @KafkaHandler
    public void handlePaymentSucceededEvent(PaymentSucceededEvent paymentSucceededEvent) {
       this.dltMessageService.register(paymentSucceededEvent);
    }

    @Transactional
    @KafkaHandler
    public void handleProcessPaymentCommand(ProcessPaymentCommand processPaymentCommand) {
        this.dltMessageService.register(processPaymentCommand);
    }

}
