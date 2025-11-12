package com.demo.common.command;

import com.demo.common.Message;
import com.demo.common.command.order.CancelOrderCommand;
import com.demo.common.command.order.CompleteOrderCommand;
import com.demo.common.command.payment.ProcessPaymentCommand;
import com.demo.common.command.product.ConfirmAvailabilityCommand;
import com.demo.common.command.product.UpdateProductsCommand;
import com.demo.common.command.shipment.ArrangeShipmentCommand;
import com.demo.common.command.shipment.CancelShipmentCommand;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type"
)
@JsonSubTypes({
        // Order commands
        @JsonSubTypes.Type(value = CancelOrderCommand.class, name = "cancelOrderCommand"),
        @JsonSubTypes.Type(value = CompleteOrderCommand.class, name = "completeOrderCommand"),

        // Payment commands
        @JsonSubTypes.Type(value = ProcessPaymentCommand.class, name = "processPaymentCommand"),

        // Product commands
        @JsonSubTypes.Type(value = ConfirmAvailabilityCommand.class, name = "confirmAvailabilityCommand"),
        @JsonSubTypes.Type(value = UpdateProductsCommand.class, name = "updateProductsCommand"),

        // Shipment commands
        @JsonSubTypes.Type(value = ArrangeShipmentCommand.class, name = "arrangeShipmentCommand"),
        @JsonSubTypes.Type(value = CancelShipmentCommand.class, name = "cancelShipmentCommand")
})
public abstract class Command extends Message {}
