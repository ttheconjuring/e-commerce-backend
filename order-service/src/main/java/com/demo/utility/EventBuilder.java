package com.demo.utility;

import com.demo.common.Type;
import com.demo.common.constant.Events;
import com.demo.common.event.Event;
import com.demo.common.event.order.OrderCancelledEvent;
import com.demo.common.event.order.OrderCompletedEvent;
import com.demo.common.event.order.OrderCreatedEvent;
import com.demo.common.payload.order.OrderCancelledPayload;
import com.demo.common.payload.order.OrderCompletedPayload;
import com.demo.common.payload.order.OrderCreatedPayload;
import com.demo.model.Status;
import com.demo.model.dto.OrderCreatedResponse;

import java.time.Instant;
import java.util.UUID;

/**
 * A static utility class for building {@link Event} objects.
 * <p>
 * This class abstracts the logic of creating and populating event payloads,
 * keeping the service and handler classes clean. Each method corresponds
 * to a specific business event (e.g., OrderCreated) and is responsible
 * for populating the event with the correct data.
 *
 * @see com.demo.service.impl.OrderServiceImpl
 * @see com.demo.component.OrderCommandsHandler
 */
public class EventBuilder {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private EventBuilder() {
    }

    /**
     * Builds an {@link Events#ORDER_CREATED} event.
     * <p>
     * This event is the first one in the saga, created right after an
     * order is saved to the database. It contains the complete order
     * details needed by the saga orchestrator to begin processing.
     *
     * @param orderCreatedResponse The DTO containing all data from the newly created order.
     * @return A fully populated {@link OrderCreatedEvent}.
     */
    public static Event orderCreatedEvent(OrderCreatedResponse orderCreatedResponse) {
        UUID correlationId = orderCreatedResponse.getOrderId();
        Event orderCreatedEvent = new OrderCreatedEvent();
        orderCreatedEvent.setId(UUID.randomUUID());
        orderCreatedEvent.setType(Type.EVENT);
        orderCreatedEvent.setName(Events.ORDER_CREATED);
        orderCreatedEvent.setTimestamp(Instant.now());
        orderCreatedEvent.setCorrelationId(correlationId);
        OrderCreatedPayload orderCreatedPayload = new OrderCreatedPayload();
        orderCreatedPayload.setOrderId(correlationId);
        orderCreatedPayload.setCustomerId(UUID.fromString(orderCreatedResponse.getCustomerId()));
        orderCreatedPayload.setProducts(orderCreatedResponse.getProducts());
        orderCreatedPayload.setTotalAmount(orderCreatedResponse.getTotalAmount());
        orderCreatedPayload.setCurrency(orderCreatedResponse.getCurrency());
        orderCreatedPayload.setPaymentMethodId(orderCreatedResponse.getPaymentMethodId());
        orderCreatedPayload.setShippingAddress(orderCreatedResponse.getShippingAddress());
        orderCreatedPayload.setCarrier(orderCreatedResponse.getCarrier());
        orderCreatedEvent.setPayload(orderCreatedPayload);
        return orderCreatedEvent;
    }

    /**
     * Builds an {@link Events#ORDER_COMPLETED} event.
     * <p>
     * This event is created when the order-service successfully processes
     * a {@code CompleteOrderCommand} from the orchestrator. It serves as
     * an acknowledgment that the order is finalized.
     *
     * @param correlationId The unique ID of the order being completed.
     * @return A fully populated {@link OrderCompletedEvent}.
     */
    public static Event orderCompletedEvent(UUID correlationId) {
        Event orderCompletedEvent = new OrderCompletedEvent();
        orderCompletedEvent.setId(UUID.randomUUID());
        orderCompletedEvent.setType(Type.EVENT);
        orderCompletedEvent.setName(Events.ORDER_COMPLETED);
        orderCompletedEvent.setTimestamp(Instant.now());
        orderCompletedEvent.setCorrelationId(correlationId);
        orderCompletedEvent.setPayload(new OrderCompletedPayload(correlationId, Status.COMPLETED.name()));
        return orderCompletedEvent;
    }

    /**
     * Builds an {@link Events#ORDER_CANCELLED} event.
     * <p>
     * This event is created when the order-service successfully processes
     * a {@code CancelOrderCommand} from the orchestrator. It serves as
     * an acknowledgment that the order has been cancelled and includes
     * the reason for the cancellation.
     *
     * @param correlationId The unique ID of the order being cancelled.
     * @param reason        A human-readable reason for the cancellation.
     * @return A fully populated {@link OrderCancelledEvent}.
     */
    public static Event orderCancelledEvent(UUID correlationId, String reason) {
        Event orderCancelledEvent = new OrderCancelledEvent();
        orderCancelledEvent.setId(UUID.randomUUID());
        orderCancelledEvent.setType(Type.EVENT);
        orderCancelledEvent.setName(Events.ORDER_CANCELLED);
        orderCancelledEvent.setTimestamp(Instant.now());
        orderCancelledEvent.setCorrelationId(correlationId);
        OrderCancelledPayload orderCancelledPayload = new OrderCancelledPayload();
        orderCancelledPayload.setOrderId(correlationId);
        orderCancelledPayload.setFinalStatus(Status.CANCELLED.name());
        orderCancelledPayload.setReason(reason);
        orderCancelledEvent.setPayload(orderCancelledPayload);
        return orderCancelledEvent;
    }

}
