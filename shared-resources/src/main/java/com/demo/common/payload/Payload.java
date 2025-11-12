package com.demo.common.payload;

import com.demo.common.payload.order.*;
import com.demo.common.payload.payment.*;
import com.demo.common.payload.product.*;
import com.demo.common.payload.shipment.*;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
)
@JsonSubTypes({
        // Order payloads
        @JsonSubTypes.Type(value = CancelOrderPayload.class, name = "cancelOrderPayload"),
        @JsonSubTypes.Type(value = CompleteOrderPayload.class, name = "completeOrderPayload"),
        @JsonSubTypes.Type(value = OrderCancelledPayload.class, name = "orderCancelledPayload"),
        @JsonSubTypes.Type(value = OrderCompletedPayload.class, name = "orderCompletedPayload"),
        @JsonSubTypes.Type(value = OrderCreatedPayload.class, name = "orderCreatedPayload"),

        // Payment payloads
        @JsonSubTypes.Type(value = PaymentFailedPayload.class, name = "paymentFailedPayload"),
        @JsonSubTypes.Type(value = PaymentSucceededPayload.class, name = "paymentSucceededPayload"),
        @JsonSubTypes.Type(value = ProcessPaymentPayload.class, name = "processPaymentPayload"),

        // Product payloads
        @JsonSubTypes.Type(value = AvailabilityConfirmedPayload.class, name = "availabilityConfirmedPayload"),
        @JsonSubTypes.Type(value = ConfirmAvailabilityPayload.class, name = "confirmAvailabilityPayload"),
        @JsonSubTypes.Type(value = ProductsShortagePayload.class, name = "productsShortagePayload"),
        @JsonSubTypes.Type(value = ProductsUpdatedPayload.class, name = "productsUpdatedPayload"),
        @JsonSubTypes.Type(value = UpdateProductsPayload.class, name = "updateProductsPayload"),

        // Shipment payloads
        @JsonSubTypes.Type(value = ArrangementFailedPayload.class, name = "arrangementFailedPayload"),
        @JsonSubTypes.Type(value = ArrangeShipmentPayload.class, name = "arrangeShipmentPayload"),
        @JsonSubTypes.Type(value = CancelShipmentPayload.class, name = "cancelShipmentPayload"),
        @JsonSubTypes.Type(value = ShipmentArrangedPayload.class, name = "shipmentArrangedPayload"),
        @JsonSubTypes.Type(value = ShipmentCancelledPayload.class, name = "shipmentCancelledPayload")
})
public interface Payload {}
