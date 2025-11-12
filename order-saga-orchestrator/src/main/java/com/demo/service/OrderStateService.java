package com.demo.service;

import com.demo.common.event.order.OrderCreatedEvent;
import com.demo.common.payload.Payload;
import com.demo.model.OrderState;
import com.demo.model.Status;

import java.util.UUID;

/**
 * Core service for orchestrating the Order Saga.
 * <p>
 * This service is responsible for managing the {@link OrderState} entity. It is
 * triggered by events from various services and is responsible for updating the
 * saga's state, collecting event payloads, and triggering subsequent commands.
 */
public interface OrderStateService {

    /**
     * Initializes a new order saga state when an order is first created.
     * This is the entry point for the entire orchestration.
     *
     * @param orderCreatedEvent The initial event containing all order details.
     */
    void create(OrderCreatedEvent orderCreatedEvent);

    /**
     * Retrieves the current state of an order saga.
     *
     * @param orderId The unique identifier (correlation ID) for the saga.
     * @return The current {@link OrderState}.
     * @throws jakarta.persistence.EntityNotFoundException if no state is found.
     */
    OrderState retrieve(UUID orderId);

    /**
     * Updates the order state upon receiving a successful payment event.
     * This method persists the payment data and triggers the next
     * step in the saga.
     *
     * @param orderId        The ID of the order.
     * @param paymentPayload The data received from the payment service.
     */
    void reflectPayment(UUID orderId, Payload paymentPayload);

    /**
     * Updates the order state to reflect a failure due to product unavailability.
     * This method moves the saga to a compensating or failed state.
     *
     * @param orderId                   The ID of the order.
     * @param productsShortagePayload The data received from the product service.
     * @return The updated {@link OrderState}.
     */
    OrderState reflectProductsUnavailability(UUID orderId, Payload productsShortagePayload);

    /**
     * Updates the order state upon successful shipment arrangement with detailed information about it.
     *
     * @param orderId                 The ID of the order.
     * @param shipmentArrangedPayload The data received from the shipping service.
     */
    void reflectShipmentArrangement(UUID orderId, Payload shipmentArrangedPayload);

    /**
     * Updates the order state upon a shipment arrangement failure with detailed information about it.
     *
     * @param orderId                  The ID of the order.
     * @param arrangementFailedPayload The failure details from the shipping service.
     * @return The updated {@link OrderState}.
     */
    OrderState reflectShipmentArrangementFailure(UUID orderId, Payload arrangementFailedPayload);

    /**
     * A general-purpose method to update the saga's status.
     * This is often called internally by other service methods.
     *
     * @param orderId   The ID of the order.
     * @param newStatus The new {@link Status} to set.
     * @return The updated {@link OrderState}.
     */
    OrderState updateStatus(UUID orderId, Status newStatus);

}
