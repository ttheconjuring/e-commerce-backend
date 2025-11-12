package com.demo.service;

import com.demo.common.payload.Payload;
import com.demo.model.Shipment;
import com.demo.model.Status;

import java.util.UUID;

/**
 * Service interface for the core business logic of managing shipments.
 * <p>
 * This contract defines the primary operations for the Shipping Service:
 * creating a new shipment record (to log an arrangement) and cancelling
 * an existing one (as a compensating action).
 *
 * @see com.demo.component.ShippingCommandsHandler
 */
public interface ShipmentService {

    /**
     * Creates and saves a new shipment record.
     * <p>
     * This method is called by the command handler to log the *outcome*
     * (either success or failure) of a shipment arrangement attempt.
     *
     * @param arrangeShipmentPayload The payload from the ArrangeShipmentCommand.
     * @param status                 The final status of the arrangement.
     * @return The newly persisted {@link Shipment} entity.
     */
    Shipment create(Payload arrangeShipmentPayload, Status status);

    /**
     * Cancels an existing shipment.
     * <p>
     * This is the **compensating transaction** for the saga. It is
     * called if a later step (like payment) fails. It finds the
     * original shipment and updates its status to {@link Status#CANCELLED}.
     *
     * @param shipmentId The ID of the shipment to cancel.
     * @param reason     A human-readable reason for the cancellation.
     * @return The updated {@link Shipment} entity.
     */
    Shipment cancel(UUID shipmentId, String reason);

}
