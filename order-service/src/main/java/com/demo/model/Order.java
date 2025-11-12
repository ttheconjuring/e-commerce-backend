package com.demo.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Represents an Order, the central aggregate root for the order-service.
 * <p>
 * This entity captures all information related to a customer's purchase,
 * including its current {@link Status}, product details, shipping information,
 * and payment details. Its lifecycle is managed by the
 * {@link com.demo.service.OrderService} and is updated based on commands
 * from the saga orchestrator.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * The unique identifier for the order (Primary Key).
     * This ID is also used as the "Correlation ID" for the entire saga.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The ID of the customer who placed the order.
     * This is a foreign key to a user in a separate Customer service (no such service exists).
     */
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    /**
     * The current status of the order (e.g., PLACED, COMPLETED, CANCELLED).
     * This field is updated by the saga orchestrator's commands.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    /**
     * A human-readable reason for why the order was cancelled, if applicable.
     * Set by the {@link com.demo.component.OrderCommandsHandler}.
     */
    @Column(name = "cancellation_reason")
    private String cancellationReason;

    /**
     * Timestamp of when the order was first created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of the last update to this order.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * A list of products included in this order.
     * <p>
     * Mapped as a one-to-many relationship with {@link OrderProduct}.
     * {@link CascadeType#ALL} and {@code orphanRemoval = true} ensure that
     * products are saved, updated, and deleted along with the order.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderProduct> products;

    /**
     * The shipping address for the order.
     * <p>
     * Mapped as a many-to-one relationship with the {@link Address} entity.
     * Fetched lazily as it may not always be needed when loading an order.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", referencedColumnName = "id")
    private Address shippingAddress;

    /**
     * The total monetary value of the order.
     * Stored as {@link BigDecimal} for financial precision.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * The three-letter ISO currency code (e.g., "USD").
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * An identifier for the payment method used (e.g., a token,
     * a credit card ID from a payment provider).
     */
    @Column(name = "payment_method_id", nullable = false)
    private String paymentMethodId;

    /**
     * The shipping carrier selected for the order (e.g., "FedEx", "UPS").
     */
    @Column(nullable = false)
    private String carrier;

}
