package com.demo.component;

import com.demo.common.command.product.ConfirmAvailabilityCommand;
import com.demo.common.command.product.UpdateProductsCommand;
import com.demo.common.event.product.AvailabilityConfirmedEvent;
import com.demo.common.event.product.ProductsShortageEvent;
import com.demo.common.event.product.ProductsUpdatedEvent;

/**
 * Defines the contract for a consumer that listens to the Dead-Letter Topics
 * (DLTs) related to the Product service.
 * <p>
 * This interface provides strongly-typed methods for each type of failed
 * product-related message. It allows the consumer to catch, deserialize,
 * and route any failed message to the {@link com.demo.service.DltMessageService}
 * for registration and later inspection.
 *
 * @see com.demo.component.impl.ProductDltHandlerImpl
 * @see com.demo.service.DltMessageService
 */
public interface ProductDltHandler {

    // Events

    /**
     * Processes a failed {@link AvailabilityConfirmedEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param availabilityConfirmedEvent The failed message.
     */
    void handleAvailabilityConfirmedEvent(AvailabilityConfirmedEvent availabilityConfirmedEvent);

    /**
     * Processes a failed {@link ProductsShortageEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param productsShortageEvent The failed message.
     */
    void handleProductsShortageEvent(ProductsShortageEvent productsShortageEvent);

    /**
     * Processes a failed {@link ProductsUpdatedEvent} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>order-saga-orchestrator</b>.
     *
     * @param productsUpdatedEvent The failed message.
     */
    void handleProductsUpdatedEvent(ProductsUpdatedEvent productsUpdatedEvent);

    // Commands

    /**
     * Processes a failed {@link ConfirmAvailabilityCommand} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>product-service</b>.
     *
     * @param confirmAvailabilityCommand The failed message.
     */
    void handleConfirmAvailabilityCommand(ConfirmAvailabilityCommand confirmAvailabilityCommand);

    /**
     * Processes a failed {@link UpdateProductsCommand} that has been routed to the DLT.
     * <p>
     * This message failed consumption by the <b>product-service</b>.
     *
     * @param updateProductsCommand The failed message.
     */
    void handleUpdateProductsCommand(UpdateProductsCommand updateProductsCommand);

}
