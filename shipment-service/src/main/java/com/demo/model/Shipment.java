package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "order_id", unique = true, nullable = false)
    private UUID orderId; // Logical link to the Order in the Order service

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "carrier", length = 100)
    @Enumerated(EnumType.STRING)
    private Carrier carrier;

    @Column(name = "tracking_number")
    private String trackingNumber;

    // --- Denormalized Address Data ---
    // This data is copied from the ArrangeShipmentCommand payload
    // to make this service fully autonomous and independent of the
    // Order Service for its data.

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "street_address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "country", nullable = false, length = 50)
    private String country;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
