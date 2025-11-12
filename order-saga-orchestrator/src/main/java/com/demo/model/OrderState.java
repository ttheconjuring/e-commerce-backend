package com.demo.model;
import com.demo.common.payload.Payload;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents the central state machine for a single order saga.
 * <p>
 * This entity acts as the "ledger" for the distributed transaction. It tracks the
 * current {@link Status} of the saga and aggregates all relevant data (payloads)
 * from the various microservices involved in the process.
 */
@Entity
@Table(name = "orders_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderState {

    /**
     * The unique identifier for the order, which also serves as the
     * "Correlation ID" for the entire saga.
     */
    @Id
    private UUID orderId;

    /**
     * The current status of the order saga.
     * This field dictates the next step or compensating action.
     */
    @Column(name = "status", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The original payload from the event that initiated the saga.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "order_created_payload", columnDefinition = "jsonb")
    private Payload orderCreatedPayload;

    /**
     * The payload received from the Payment service upon successful payment.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_succeeded_payload", columnDefinition = "jsonb")
    private Payload paymentSucceededPayload;

    /**
     * The payload received from the Payment service upon failed payment.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_failed_payload", columnDefinition = "jsonb")
    private Payload paymentFailedPayload;

    /**
     * The payload received from the Shipping service upon successful arrangement.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipment_arranged_payload", columnDefinition = "jsonb")
    private Payload shipmentArrangedPayload;

    /**
     * The payload received from the Shipping service upon failed arrangement.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "arrangement_failed_payload", columnDefinition = "jsonb")
    private Payload arrangementFailedPayload;

    /**
     * A human-readable reason for the saga's failure, if applicable.
     * Used for debugging and business intelligence.
     */
    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * Timestamp of when the saga was initiated.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Timestamp of the last update to this state.
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
