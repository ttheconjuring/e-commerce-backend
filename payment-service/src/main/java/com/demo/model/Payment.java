package com.demo.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a payment transaction within the system.
 * <p>
 * This entity is the aggregate root for the Payment Service. It logs every
 * payment attempt, its status (e.g., SUCCEEDED, FAILED), and any
 * relevant transaction data from the payment gateway.
 *
 * @see com.demo.service.PaymentService
 * @see com.demo.component.PaymentCommandsHandler
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /**
     * The unique primary key for this payment record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The ID of the order this payment is for.
     * <p>
     * This is a "logical" foreign key, as the {@link Order} entity lives in
     * the separate order-service. It's also the saga's "Correlation ID".
     * The `unique = true` constraint ensures only one payment record per order.
     */
    @Column(name = "order_id", unique = true, nullable = false)
    private UUID orderId; // Logical link to the Order in the Order service

    /**
     * The total amount to be charged.
     * Stored as {@link BigDecimal} for financial precision.
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * The three-letter ISO currency code (e.g., "USD").
     */
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    /**
     * The outcome of the payment attempt (e.g., PAYMENT_SUCCEEDED, PAYMENT_FAILED).
     * This status is sent back to the saga orchestrator.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private Status status;

    /**
     * A human-readable reason for payment failure, if applicable.
     * (e.g., "Insufficient funds", "Card declined").
     */
    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * The payment method used (e.g., "CREDIT_CARD", "PAYPAL").
     * This might be derived from the payment_method_id.
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * The unique transaction ID returned from the external payment gateway
     * (e.g., Stripe, PayPal). This is crucial for auditing and refunds.
     */
    @Column(name = "transaction_id")
    private String transactionId;

    /**
     * Timestamp of when this payment record was created (i.e., when the
     * payment attempt was initiated). Managed by Hibernate.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of the last update to this payment record.
     * Managed by Hibernate.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
