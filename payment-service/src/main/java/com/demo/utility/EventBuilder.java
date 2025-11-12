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

/**
 * A static utility class for building {@link Event} objects related to payment outcomes.
 * <p>
 * This class abstracts the logic of creating and populating event payloads,
 * keeping the {@link com.demo.component.PaymentCommandsHandler} clean and focused.
 * Each method corresponds to a specific payment result.
 */
public class EventBuilder {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private EventBuilder() {}

    /**
     * Builds an {@link Events#PAYMENT_SUCCEEDED} event (Happy Path).
     * <p>
     * This event is created when the payment-service successfully processes
     * a payment. It notifies the saga orchestrator that it can proceed
     * to the next step.
     *
     * @param correlationId The saga's correlation ID (the Order ID).
     * @param transactionId The unique transaction ID from the payment gateway.
     * @return A fully populated {@link PaymentSucceededEvent}.
     */
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

    /**
     * Builds an {@link Events#PAYMENT_FAILED} event (Failure Path).
     * <p>
     * This event is created when the payment-service fails to process a
     * payment. It notifies the saga orchestrator that a failure has
     * occurred, which will likely trigger compensating actions.
     *
     * @param correlationId The saga's correlation ID (the Order ID).
     * @param failureReason A human-readable reason for the payment failure.
     * @return A fully populated {@link PaymentFailedEvent}.
     */
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
