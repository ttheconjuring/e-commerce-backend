package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a shipment, the central aggregate root for the Shipping Service.
 * <p>
 * This entity tracks the lifecycle of a single shipment, from its
 * arrangement request to its eventual status. It contains a denormalized copy of the recipient's
 * address to be fully independent of the Order Service.
 *
 * @see com.demo.service.ShipmentService
 * @see com.demo.component.ShippingCommandsHandler
 */
@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    /**
     * The unique identifier (Primary Key) for this shipment record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The ID of the order this shipment is for.
     * <p>
     * This is a "logical" foreign key, as the {@link Order} entity lives in
     * the separate order-service. It's also the saga's "Correlation ID".
     * The `unique = true` constraint ensures only one shipment record per order.
     */
    @Column(name = "order_id", unique = true, nullable = false)
    private UUID orderId; // Logical link to the Order in the Order service

    /**
     * The current status of the shipment.
     * This status is sent back to the saga orchestrator.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    /**
     * A human-readable reason for why the shipment *arrangement* failed,
     * if applicable (e.g., "Invalid address", "No carriers available").
     */
    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * A human-readable reason for why the shipment was *cancelled*,
     * if applicable. This is typically set during a compensating
     * transaction (e.g., "Payment failed").
     */
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    /**
     * The shipping carrier assigned to this shipment (e.g., FEDEX, UPS, DHL).
     */
    @Column(name = "carrier", length = 100)
    @Enumerated(EnumType.STRING)
    private Carrier carrier;

    /**
     * The tracking number provided by the carrier after a successful
     * arrangement. This is a key piece of data sent back to the orchestrator.
     */
    @Column(name = "tracking_number")
    private String trackingNumber;

    // --- Denormalized Address Data ---
    // This data is copied from the ArrangeShipmentCommand payload
    // to make this service fully autonomous and independent of the
    // Order Service for its data.

    /**
     * The full name of the recipient. (Denormalized)
     */
    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    /**
     * The street address line. (Denormalized)
     */
    @Column(name = "street_address", nullable = false)
    private String address;

    /**
     * The city name. (Denormalized)
     */
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    /**
     * The postal or ZIP code. (Denormalized)
     */
    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    /**
     * The country name. (Denormalized)
     */
    @Column(name = "country", nullable = false, length = 50)
    private String country;

    /**
     * Timestamp of when this shipment record was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of the last update to this shipment record.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
