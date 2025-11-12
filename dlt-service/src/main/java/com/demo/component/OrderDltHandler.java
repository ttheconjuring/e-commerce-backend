package com.demo.component;

import com.demo.common.command.order.CancelOrderCommand;
import com.demo.common.command.order.CompleteOrderCommand;
import com.demo.common.event.order.OrderCancelledEvent;
import com.demo.common.event.order.OrderCompletedEvent;
import com.demo.common.event.order.OrderCreatedEvent;

/**
 * Defines the contract for a consumer that listens to the Dead-Letter Topics
 * (DLTs) related to the Order and Saga Orchestrator services.
 * <p>
 * Its purpose is to provide a strongly-typed method for each type of
 * failed message. This allows the consumer to catch, deserialize, and route
 * any failed message to the {@link com.demo.service.DltMessageService}
 * for registration and later inspection.
 *
 * @see com.demo.component.impl.OrderDltHandlerImpl
 * @see com.demo.service.DltMessageService
 */
public interface OrderDltHandler {

    // Events

    /**
     * Processes a failed {@link OrderCreatedEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param orderCreatedEvent The failed message.
     */
    void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent);

    /**
     * Processes a failed {@link OrderCompletedEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param orderCompletedEvent The failed message.
     */
    void handleOrderCompletedEvent(OrderCompletedEvent orderCompletedEvent);

    /**
     * Processes a failed {@link OrderCancelledEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param orderCancelledEvent The failed message.
     */
    void handleOrderCancelledEvent(OrderCancelledEvent orderCancelledEvent);

    // Commands

    /**
     * Processes a failed {@link CancelOrderCommand} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-service</b>.
     *
     * @param cancelOrderCommand The failed message.
     */
    void handleCancelOrderCommand(CancelOrderCommand cancelOrderCommand);

    /**
     * Processes a failed {@link CompleteOrderCommand} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-service</b>.
     *
     * @param completeOrderCommand The failed message.
     */
    void handleCompleteOrderCommand(CompleteOrderCommand completeOrderCommand);

}
