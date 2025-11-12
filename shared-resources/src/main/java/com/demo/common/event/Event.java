package com.demo.common.event;

import com.demo.common.Message;
import com.demo.common.event.order.OrderCancelledEvent;
import com.demo.common.event.order.OrderCompletedEvent;
import com.demo.common.event.order.OrderCreatedEvent;
import com.demo.common.event.payment.PaymentFailedEvent;
import com.demo.common.event.payment.PaymentSucceededEvent;
import com.demo.common.event.product.AvailabilityConfirmedEvent;
import com.demo.common.event.product.ProductsShortageEvent;
import com.demo.common.event.product.ProductsUpdatedEvent;
import com.demo.common.event.shipment.ArrangementFailedEvent;
import com.demo.common.event.shipment.ShipmentArrangedEvent;
import com.demo.common.event.shipment.ShipmentCancelledEvent;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
)
@JsonSubTypes({
        // Order events
        @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "orderCancelledEvent"),
        @JsonSubTypes.Type(value = OrderCompletedEvent.class, name = "orderCompletedEvent"),
        @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "orderCreatedEvent"),

        // Payment events
        @JsonSubTypes.Type(value = PaymentFailedEvent.class, name = "paymentFailedEvent"),
        @JsonSubTypes.Type(value = PaymentSucceededEvent.class, name = "paymentSucceededEvent"),

        // Product events
        @JsonSubTypes.Type(value = AvailabilityConfirmedEvent.class, name = "availabilityConfirmedEvent"),
        @JsonSubTypes.Type(value = ProductsShortageEvent.class, name = "productsShortageEvent"),
        @JsonSubTypes.Type(value = ProductsUpdatedEvent.class, name = "productsUpdatedEvent"),

        // Shipment events
        @JsonSubTypes.Type(value = ArrangementFailedEvent.class, name = "arrangementFailedEvent"),
        @JsonSubTypes.Type(value = ShipmentArrangedEvent.class, name = "shipmentArrangedEvent"),
        @JsonSubTypes.Type(value = ShipmentCancelledEvent.class, name = "shipmentCancelledEvent")
})
public abstract class Event extends Message {}
