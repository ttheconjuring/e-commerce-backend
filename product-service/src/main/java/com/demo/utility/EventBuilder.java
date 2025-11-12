package com.demo.utility;

import com.demo.common.Type;
import com.demo.common.constant.Events;
import com.demo.common.dto.InsufficientProductDTO;
import com.demo.common.event.Event;
import com.demo.common.event.product.AvailabilityConfirmedEvent;
import com.demo.common.event.product.ProductsShortageEvent;
import com.demo.common.event.product.ProductsUpdatedEvent;
import com.demo.common.payload.product.AvailabilityConfirmedPayload;
import com.demo.common.payload.product.ProductsShortagePayload;
import com.demo.common.payload.product.ProductsUpdatedPayload;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * A static utility class for building {@link Event} objects related to product
 * and inventory outcomes.
 * <p>
 * This class abstracts the logic of creating and populating event payloads,
 * keeping the {@link com.demo.component.ProductCommandsHandler} clean and
 * focused on orchestration logic. Each method corresponds to a specific
 * inventory-related event.
 */
public class EventBuilder {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private EventBuilder() {}

    /**
     * Builds an {@link Events#AVAILABILITY_CONFIRMED} event (Happy Path).
     * <p>
     * This event is created when the {@link com.demo.service.ProductService}
     * confirms that all requested products are in stock. It notifies the
     * saga orchestrator that it can proceed to the next step (e.g., arranging
     * shipment or processing payment).
     *
     * @param correlationId The saga's correlation ID (the Order ID).
     * @return A fully populated {@link AvailabilityConfirmedEvent}.
     */
    public static Event availabilityConfirmedEvent(UUID correlationId) {
        Event availabilityConfirmedEvent = new AvailabilityConfirmedEvent();
        availabilityConfirmedEvent.setId(UUID.randomUUID());
        availabilityConfirmedEvent.setType(Type.EVENT);
        availabilityConfirmedEvent.setName(Events.AVAILABILITY_CONFIRMED);
        availabilityConfirmedEvent.setTimestamp(Instant.now());
        availabilityConfirmedEvent.setCorrelationId(correlationId);
        availabilityConfirmedEvent.setPayload(new AvailabilityConfirmedPayload(correlationId));
        return availabilityConfirmedEvent;
    }

    /**
     * Builds an {@link Events#PRODUCTS_UPDATED} event.
     * <p>
     * This event is created as an acknowledgment after the
     * {@code UpdateProductsCommand} has been successfully processed
     * (i.e., stock has been permanently decremented).
     *
     * @param correlationId The saga's correlation ID (the Order ID).
     * @return A fully populated {@link ProductsUpdatedEvent}.
     */
    public static Event productsUpdatedEvent(UUID correlationId) {
        Event productsUpdatedEvent = new ProductsUpdatedEvent();
        productsUpdatedEvent.setId(UUID.randomUUID());
        productsUpdatedEvent.setType(Type.EVENT);
        productsUpdatedEvent.setName(Events.PRODUCTS_UPDATED);
        productsUpdatedEvent.setTimestamp(Instant.now());
        productsUpdatedEvent.setCorrelationId(correlationId);
        productsUpdatedEvent.setPayload(new ProductsUpdatedPayload(correlationId));
        return productsUpdatedEvent;
    }

    /**
     * Builds an {@link Events#PRODUCTS_SHORTAGE} event (Failure Path).
     * <p>
     * This event is created when the {@link com.demo.service.ProductService}
     * determines that one or more products are unavailable in the requested
     * quantity. It notifies the saga orchestrator of the failure, which
     * will trigger compensating actions (e.g., cancelling the order).
     *
     * @param correlationId        The saga's correlation ID (the Order ID).
     * @param insufficientProducts A list of DTOs detailing which products
     * are out of stock.
     * @return A fully populated {@link ProductsShortageEvent}.
     */
    public static Event productsShortageEvent(UUID correlationId, List<InsufficientProductDTO> insufficientProducts) {
        Event productsShortageEvent = new ProductsShortageEvent();
        productsShortageEvent.setId(UUID.randomUUID());
        productsShortageEvent.setType(Type.EVENT);
        productsShortageEvent.setName(Events.PRODUCTS_SHORTAGE);
        productsShortageEvent.setTimestamp(Instant.now());
        productsShortageEvent.setCorrelationId(correlationId);
        ProductsShortagePayload productsShortagePayload = new ProductsShortagePayload();
        productsShortagePayload.setOrderId(correlationId);
        productsShortagePayload.setReason(buildReason(insufficientProducts));
        productsShortagePayload.setOutOfStockProducts(insufficientProducts);
        productsShortageEvent.setPayload(productsShortagePayload);
        return productsShortageEvent;
    }

    /**
     * Private helper method to build a human-readable string
     * detailing the product shortages.
     * <p>
     * This string is intended for the {@link ProductsShortagePayload}
     * to provide a clear, log-friendly reason for the failure.
     *
     * @param insufficientProducts The list of products that are out of stock.
     * @return A formatted string (e.g., "The requested quantity exceeds...
     * 1. [Product ID] (requested: 10, available: 5)").
     */
    private static String buildReason(List<InsufficientProductDTO> insufficientProducts) {
        int insufficientProductsCount = insufficientProducts.size();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("The requested quantity exceeds the available quantity for %d products.%n", insufficientProductsCount));
        for (int i = 1; i <= insufficientProductsCount; i++) {
            InsufficientProductDTO insufficientProductDTO = insufficientProducts.get(i - 1);
            sb.append(String.format("%d. %s (requested: %d, available: %d)%n", i,
                    insufficientProductDTO.getProductId(),
                    insufficientProductDTO.getRequestedQuantity(),
                    insufficientProductDTO.getAvailableQuantity()));
        }
        return sb.toString();
    }

}
