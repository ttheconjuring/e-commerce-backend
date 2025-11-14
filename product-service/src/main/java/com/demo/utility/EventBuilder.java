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

public class EventBuilder {

    private EventBuilder() {
        throw new AssertionError("EventBuilder class should not be instantiated.");
    }

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
