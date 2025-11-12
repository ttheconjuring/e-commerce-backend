package com.demo.component;

import com.demo.common.command.payment.ProcessPaymentCommand;
import com.demo.common.event.payment.PaymentFailedEvent;
import com.demo.common.event.payment.PaymentSucceededEvent;

/**
 * Defines the contract for a consumer that listens to the Dead-Letter Topics
 * (DLTs) related to the Payment service.
 * <p>
 * This interface provides strongly-typed methods for each type of failed
 * payment-related message. It allows the consumer to catch, deserialize,
 * and route any failed message to the {@link com.demo.service.DltMessageService}
 * for registration and later inspection.
 *
 * @see com.demo.component.impl.PaymentDltHandlerImpl
 * @see com.demo.service.DltMessageService
 */
public interface PaymentDltHandler {

    // Events

    /**
     * Processes a failed {@link PaymentFailedEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param paymentFailedEvent The failed message.
     */
    void handlePaymentFailedEvent(PaymentFailedEvent paymentFailedEvent);

    /**
     * Processes a failed {@link PaymentSucceededEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param paymentSucceededEvent The failed message.
     */
    void handlePaymentSucceededEvent(PaymentSucceededEvent paymentSucceededEvent);

    // Commands

    /**
     * Processes a failed {@link ProcessPaymentCommand} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>payment-service</b>.
     *
     * @param processPaymentCommand The failed message.
     */
    void handleProcessPaymentCommand(ProcessPaymentCommand processPaymentCommand);

}
