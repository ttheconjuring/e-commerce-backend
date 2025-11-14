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


public class EventBuilder {


    private EventBuilder() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

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
