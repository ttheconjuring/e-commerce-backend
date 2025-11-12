package com.demo.utility;

import com.demo.model.DltMessage;
import com.demo.model.DltMessageDTO;

/**
 * A static utility class providing helper methods for the DLT service.
 * <p>
 * This class includes mappers for converting entities to DTOs and
 * logic for generating human-readable error descriptions.
 */
public class Utils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Utils() {}

    /**
     * Generates a human-readable description of a processing failure.
     * <p>
     * This method acts as a central registry, mapping a failed message's
     * class name (e.g., "OrderCreatedEvent") to the name of the microservice
     * that was *supposed* to consume it (e.g., "order-saga-orchestrator").
     * <p>
     * The output is a clear, concise summary of the failure, such as:
     * "ORDER-SAGA-ORCHESTRATOR couldn't process ORDER_CREATED."
     * This is invaluable for quickly diagnosing problems from the DLT.
     *
     * @param messageName The {@link Class#getSimpleName()} of the failed message.
     * @return A formatted, human-readable error description.
     * @throws IllegalArgumentException if the messageName is not recognized.
     */
    public static String determineDescription(String messageName) {
        String result = "%s couldn't process %s.";
        String eventOrCommandName;
        String serviceName;

        // This switch maps a failed message to its intended consumer service.
        switch (messageName) {
            // --- Events consumed by the Saga Orchestrator ---
                // Order Events
            case "OrderCancelledEvent":
                eventOrCommandName = "ORDER_CANCELLED";
                serviceName = "order-saga-orchestrator";
                break;
            case "OrderCompletedEvent":
                eventOrCommandName = "ORDER_COMPLETED";
                serviceName = "order-saga-orchestrator";
                break;
            case "OrderCreatedEvent":
                eventOrCommandName = "ORDER_CREATED";
                serviceName = "order-saga-orchestrator";
                break;
                // Payment Events
            case "PaymentFailedEvent":
                eventOrCommandName = "PAYMENT_FAILED";
                serviceName = "order-saga-orchestrator";
                break;
            case "PaymentSucceededEvent":
                eventOrCommandName = "PAYMENT_SUCCEEDED";
                serviceName = "order-saga-orchestrator";
                break;
                // Product Events
            case "AvailabilityConfirmedEvent":
                eventOrCommandName = "AVAILABILITY_CONFIRMED";
                serviceName = "order-saga-orchestrator";
                break;
            case "ProductsShortageEvent":
                eventOrCommandName = "PRODUCTS_SHORTAGE";
                serviceName = "order-saga-orchestrator";
                break;
            case "ProductsUpdatedEvent":
                eventOrCommandName = "PRODUCTS_UPDATED";
                serviceName = "order-saga-orchestrator";
                break;
                // Shipment Events
            case "ArrangementFailedEvent":
                eventOrCommandName = "ARRANGEMENT_FAILED";
                serviceName = "order-saga-orchestrator";
                break;
            case "ShipmentArrangedEvent":
                eventOrCommandName = "SHIPMENT_ARRANGED";
                serviceName = "order-saga-orchestrator";
                break;
            case "ShipmentCancelledEvent":
                eventOrCommandName = "SHIPMENT_CANCELLED";
                serviceName = "order-saga-orchestrator";
                break;

            // --- Commands consumed by the Order Service ---
            case "CancelOrderCommand":
                eventOrCommandName = "CANCEL_ORDER";
                serviceName = "order-service";
                break;
            case "CompleteOrderCommand":
                eventOrCommandName = "COMPLETE_ORDER";
                serviceName = "order-service";
                break;

            // --- Commands consumed by the Payment Service ---
            case "ProcessPaymentCommand":
                eventOrCommandName = "PROCESS_PAYMENT";
                serviceName = "payment-service";
                break;

            // --- Commands consumed by the Product Service
            case "ConfirmAvailabilityCommand":
                eventOrCommandName = "CONFIRM_AVAILABILITY";
                serviceName = "product-service";
                break;
            case "UpdateProductsCommand":
                eventOrCommandName = "UPDATE_PRODUCTS";
                serviceName = "product-service";
                break;

            // --- Commands consumed by the Shipment Service
            case "ArrangeShipmentCommand":
                eventOrCommandName = "ARRANGE_SHIPMENT";
                serviceName = "shipment-service";
                break;
            default:
                throw new IllegalArgumentException("Unknown problematic object name: " + messageName);
        }

        return String.format(result, serviceName.toUpperCase(), eventOrCommandName);
    }

    /**
     * Converts a {@link DltMessage} entity to a {@link DltMessageDTO}.
     * <p>
     * This method maps the core fields from the database entity to a
     * Data Transfer Object (DTO) suitable for sending over the API.
     * It's used to build the list for the {@link com.demo.controller.ErrorController}.
     * <p>
     * (Note: There is a typo in the method name, "covertToDto".
     * It should ideally be "convertToDto".)
     *
     * @param dltMessage The persisted {@link DltMessage} entity from the database.
     * @return A new {@link DltMessageDTO} object.
     */
    public static DltMessageDTO covertToDto(DltMessage dltMessage) {
        DltMessageDTO dto = new DltMessageDTO();
        dto.setId(dltMessage.getId());
        dto.setMessageId(dltMessage.getMessageId());
        dto.setDescription(dltMessage.getDescription());
        dto.setReceivedAt(dltMessage.getReceivedAt());
        return dto;
    }

}
