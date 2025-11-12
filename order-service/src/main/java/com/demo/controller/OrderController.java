package com.demo.controller;

import com.demo.model.dto.CreateOrderRequest;
import com.demo.model.dto.OrderCreatedResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * REST API Contract for the Order Service.
 * <p>
 * This interface defines the public endpoints for creating and managing orders.
 * All business logic is delegated to the {@link com.demo.service.OrderService}.
 */
public interface OrderController {

    /**
     * Endpoint to create a new order.
     * <p>
     * This is the primary entry point for initiating an order. The request
     * body is validated and then passed to the service layer to
     * begin the order creation process.
     *
     * @param createOrderRequest A {@link @Valid} DTO containing all order
     * details (e.g., customer, products).
     * @return A {@link ResponseEntity} with status 201 (Created) and an
     * {@link OrderCreatedResponse} body containing the new order's ID.
     */
    ResponseEntity<OrderCreatedResponse> createOrder(CreateOrderRequest createOrderRequest);

    /**
     * Endpoint to check the status of an existing order.
     *
     * @param id The unique identifier (UUID) of the order.
     * @return A {@link ResponseEntity} with:
     * <ul>
     * <li>200 (OK) and the order status (e.g., "PLACED", "COMPLETE").</li>
     * <li>200 (OK) and a formatted string (Status + Reason) if the order is "CANCELLED".</li>
     * <li>404 (Not Found) if the order does not exist.</li>
     * </ul>
     */
    ResponseEntity<String> checkOrderStatus(UUID id);

}
