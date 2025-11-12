package com.demo.service;

import com.demo.model.Order;
import com.demo.model.Status;
import com.demo.model.dto.CreateOrderRequest;
import com.demo.model.dto.OrderCreatedResponse;

import java.util.UUID;

/**
 * Service interface for the core business logic of managing orders.
 * <p>
 * This contract defines the primary operations for an order: creation,
 * retrieval, and status modification.
 */
public interface OrderService {

    /**
     * Creates a new order based on a customer's request.
     * <p>
     * This is the main transactional method for initiating an order. It
     * persists the order to the database and creates an
     * {@link com.demo.model.OutboxEvent} to be published, all atomically.
     *
     * @param request The DTO containing all order details.
     * @return An {@link OrderCreatedResponse} DTO with details of the created order.
     */
    OrderCreatedResponse create(CreateOrderRequest request);

    /**
     * Retrieves a single order by its unique identifier.
     *
     * @param orderId The UUID of the order to retrieve.
     * @return The {@link Order} entity.
     * @throws java.util.NoSuchElementException if no order is found with the given ID.
     */
    Order retrieve(UUID orderId);

    /**
     * Updates the status of an existing order.
     * <p>
     * This is called by a command handler
     * in response to events from the saga orchestrator.
     *
     * @param orderId   The UUID of the order to update.
     * @param newStatus The new {@link Status} to set.
     */
    void updateStatus(UUID orderId, Status newStatus);

    /**
     * Sets the cancellation reason for a failed or cancelled order.
     * <p>
     * This is called by a command handler when an order fails, providing
     * a human-readable reason for the failure.
     *
     * @param orderId The UUID of the order.
     * @param reason  The text explaining the cancellation.
     */
    void setCancellationReason(UUID orderId, String reason);

}
