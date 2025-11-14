package com.demo.utility;

import com.demo.common.Type;
import com.demo.common.constant.Events;
import com.demo.common.event.Event;
import com.demo.common.event.payment.PaymentFailedEvent;
import com.demo.common.event.payment.PaymentSucceededEvent;
import com.demo.common.payload.payment.PaymentFailedPayload;
import com.demo.common.payload.payment.PaymentSucceededPayload;

import java.time.Instant;
import java.util.UUID;

public class EventBuilder {

    private EventBuilder() {}

    public static Event paymentSucceededEvent(UUID correlationId, String transactionId) {
        Event paymentSucceededEvent = new PaymentSucceededEvent();
        paymentSucceededEvent.setId(UUID.randomUUID());
        paymentSucceededEvent.setType(Type.EVENT);
        paymentSucceededEvent.setName(Events.PAYMENT_SUCCEEDED);
        paymentSucceededEvent.setTimestamp(Instant.now());
        paymentSucceededEvent.setCorrelationId(correlationId);
        PaymentSucceededPayload paymentSucceededPayload = new PaymentSucceededPayload();
        paymentSucceededPayload.setOrderId(correlationId);
        paymentSucceededPayload.setTransactionId(transactionId);
        paymentSucceededEvent.setPayload(paymentSucceededPayload);
        return paymentSucceededEvent;
    }

    public static Event paymentFailedEvent(UUID correlationId, String failureReason) {
        Event paymentFailedEvent = new PaymentFailedEvent();
        paymentFailedEvent.setId(UUID.randomUUID());
        paymentFailedEvent.setType(Type.EVENT);
        paymentFailedEvent.setName(Events.PAYMENT_FAILED);
        paymentFailedEvent.setTimestamp(Instant.now());
        paymentFailedEvent.setCorrelationId(correlationId);
        PaymentFailedPayload paymentFailedPayload = new PaymentFailedPayload();
        paymentFailedPayload.setOrderId(correlationId);
        paymentFailedPayload.setReason(failureReason);
        paymentFailedPayload.setProcessedAt(Instant.now());
        paymentFailedEvent.setPayload(paymentFailedPayload);
        return paymentFailedEvent;
    }

}
