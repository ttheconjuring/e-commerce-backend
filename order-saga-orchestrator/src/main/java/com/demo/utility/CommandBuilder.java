package com.demo.utility;

import com.demo.common.Type;
import com.demo.common.command.Command;
import com.demo.common.command.order.CancelOrderCommand;
import com.demo.common.command.order.CompleteOrderCommand;
import com.demo.common.command.payment.ProcessPaymentCommand;
import com.demo.common.command.product.ConfirmAvailabilityCommand;
import com.demo.common.command.product.UpdateProductsCommand;
import com.demo.common.command.shipment.ArrangeShipmentCommand;
import com.demo.common.command.shipment.CancelShipmentCommand;
import com.demo.common.constant.Commands;
import com.demo.common.dto.OrderProductDTO;
import com.demo.common.dto.ProductQuantityDTO;
import com.demo.common.event.order.OrderCreatedEvent;
import com.demo.common.payload.order.CancelOrderPayload;
import com.demo.common.payload.order.CompleteOrderPayload;
import com.demo.common.payload.order.OrderCreatedPayload;
import com.demo.common.payload.payment.ProcessPaymentPayload;
import com.demo.common.payload.product.ConfirmAvailabilityPayload;
import com.demo.common.payload.product.UpdateProductsPayload;
import com.demo.common.payload.shipment.ArrangeShipmentPayload;
import com.demo.common.payload.shipment.CancelShipmentPayload;
import com.demo.common.payload.shipment.ShipmentArrangedPayload;
import com.demo.model.OrderState;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommandBuilder {

    private CommandBuilder() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static Command cancelOrderCommand(UUID correlationId, String reason) {
        Command cancelOrderCommand = new CancelOrderCommand();
        cancelOrderCommand.setId(UUID.randomUUID());
        cancelOrderCommand.setType(Type.COMMAND);
        cancelOrderCommand.setName(Commands.CANCEL_ORDER);
        cancelOrderCommand.setTimestamp(Instant.now());
        cancelOrderCommand.setCorrelationId(correlationId);
        CancelOrderPayload cancelOrderPayload = new CancelOrderPayload();
        cancelOrderPayload.setOrderId(correlationId);
        cancelOrderPayload.setReason(reason);
        cancelOrderCommand.setPayload(cancelOrderPayload);
        return cancelOrderCommand;
    }

    public static Command confirmAvailabilityCommand(OrderCreatedEvent orderCreatedEvent) {
        UUID correlationId = orderCreatedEvent.getCorrelationId();
        OrderCreatedPayload orderCreatedPayload = (OrderCreatedPayload) orderCreatedEvent.getPayload();
        List<ProductQuantityDTO> productsToCheck = CommandBuilder.convertToProductQuantityList(orderCreatedPayload.getProducts());
        Command confirmAvailabilityCommand = new ConfirmAvailabilityCommand();
        confirmAvailabilityCommand.setId(UUID.randomUUID());
        confirmAvailabilityCommand.setType(Type.COMMAND);
        confirmAvailabilityCommand.setName(Commands.CONFIRM_AVAILABILITY);
        confirmAvailabilityCommand.setTimestamp(Instant.now());
        confirmAvailabilityCommand.setCorrelationId(correlationId);
        ConfirmAvailabilityPayload confirmAvailabilityPayload = new ConfirmAvailabilityPayload();
        confirmAvailabilityPayload.setOrderId(correlationId);
        confirmAvailabilityPayload.setProductsToCheck(productsToCheck);
        confirmAvailabilityCommand.setPayload(confirmAvailabilityPayload);
        return confirmAvailabilityCommand;
    }

    public static Command updateProductsCommand(UUID correlationId, OrderState orderState) {
        Command updateProductsCommand = new UpdateProductsCommand();
        updateProductsCommand.setId(UUID.randomUUID());
        updateProductsCommand.setType(Type.COMMAND);
        updateProductsCommand.setName(Commands.UPDATE_PRODUCTS);
        updateProductsCommand.setTimestamp(Instant.now());
        updateProductsCommand.setCorrelationId(correlationId);
        OrderCreatedPayload orderCreatedPayload = (OrderCreatedPayload) orderState.getOrderCreatedPayload();
        List<ProductQuantityDTO> productsToDecrement = CommandBuilder.convertToProductQuantityList(orderCreatedPayload.getProducts());
        UpdateProductsPayload updateProductsPayload = new UpdateProductsPayload();
        updateProductsPayload.setOrderId(correlationId);
        updateProductsPayload.setProductsToDecrement(productsToDecrement);
        updateProductsCommand.setPayload(updateProductsPayload);
        return updateProductsCommand;
    }

    public static Command completeOrderCommand(UUID correlationId) {
        Command completeOrderCommand = new CompleteOrderCommand();
        completeOrderCommand.setId(UUID.randomUUID());
        completeOrderCommand.setType(Type.COMMAND);
        completeOrderCommand.setName(Commands.COMPLETE_ORDER);
        completeOrderCommand.setTimestamp(Instant.now());
        completeOrderCommand.setCorrelationId(correlationId);
        completeOrderCommand.setPayload(new CompleteOrderPayload(correlationId));
        return completeOrderCommand;
    }

    public static Command processPaymentCommand(UUID correlationId, OrderState orderState) {
        OrderCreatedPayload orderCreatedPayload = (OrderCreatedPayload) orderState.getOrderCreatedPayload();
        Command processPaymentCommand = new ProcessPaymentCommand();
        processPaymentCommand.setId(UUID.randomUUID());
        processPaymentCommand.setType(Type.COMMAND);
        processPaymentCommand.setName(Commands.PROCESS_PAYMENT);
        processPaymentCommand.setTimestamp(Instant.now());
        processPaymentCommand.setCorrelationId(correlationId);
        ProcessPaymentPayload processPaymentPayload = new ProcessPaymentPayload();
        processPaymentPayload.setOrderId(correlationId);
        processPaymentPayload.setTotalAmount(orderCreatedPayload.getTotalAmount());
        processPaymentPayload.setCurrency(orderCreatedPayload.getCurrency());
        processPaymentPayload.setPaymentMethodId(orderCreatedPayload.getPaymentMethodId());
        processPaymentCommand.setPayload(processPaymentPayload);
        return processPaymentCommand;
    }

    public static Command arrangeShipmentCommand(UUID correlationId, OrderState orderState) {
        OrderCreatedPayload orderCreatedPayload = (OrderCreatedPayload) orderState.getOrderCreatedPayload();
        List<ProductQuantityDTO> products = CommandBuilder.convertToProductQuantityList(orderCreatedPayload.getProducts());
        Command arrangeShipmentCommand = new ArrangeShipmentCommand();
        arrangeShipmentCommand.setId(UUID.randomUUID());
        arrangeShipmentCommand.setType(Type.COMMAND);
        arrangeShipmentCommand.setName(Commands.ARRANGE_SHIPMENT);
        arrangeShipmentCommand.setTimestamp(Instant.now());
        arrangeShipmentCommand.setCorrelationId(correlationId);
        ArrangeShipmentPayload arrangeShipmentPayload = new ArrangeShipmentPayload();
        arrangeShipmentPayload.setOrderId(correlationId);
        arrangeShipmentPayload.setCustomerId(orderCreatedPayload.getCustomerId());
        arrangeShipmentPayload.setProducts(products);
        arrangeShipmentPayload.setShippingAddress(orderCreatedPayload.getShippingAddress());
        arrangeShipmentCommand.setPayload(arrangeShipmentPayload);
        return arrangeShipmentCommand;
    }

    public static Command cancelShipmentCommand(UUID correlationId, OrderState orderState, String reason) {
        Command cancelShipmentCommand = new CancelShipmentCommand();
        cancelShipmentCommand.setId(UUID.randomUUID());
        cancelShipmentCommand.setType(Type.COMMAND);
        cancelShipmentCommand.setName(Commands.CANCEL_SHIPMENT);
        cancelShipmentCommand.setTimestamp(Instant.now());
        cancelShipmentCommand.setCorrelationId(correlationId);
        CancelShipmentPayload cancelShipmentPayload = new CancelShipmentPayload();
        cancelShipmentPayload.setOrderId(correlationId);
        ShipmentArrangedPayload shipmentArrangedPayload = (ShipmentArrangedPayload) orderState.getShipmentArrangedPayload();
        cancelShipmentPayload.setShipmentId(shipmentArrangedPayload.getShipmentId());
        cancelShipmentPayload.setReason("The order was not paid: " + reason);
        cancelShipmentCommand.setPayload(cancelShipmentPayload);
        return cancelShipmentCommand;
    }

    private static List<ProductQuantityDTO> convertToProductQuantityList(List<OrderProductDTO> orderProductDTOs) {
        List<ProductQuantityDTO> productsToCheck = new ArrayList<>();
        for (OrderProductDTO orderProductDTO : orderProductDTOs) {
            ProductQuantityDTO productQuantityDTO = new ProductQuantityDTO();
            productQuantityDTO.setProductId(orderProductDTO.getProductId());
            productQuantityDTO.setQuantity(orderProductDTO.getQuantity());
            productsToCheck.add(productQuantityDTO);
        }
        return productsToCheck;
    }

}
