package com.demo.model;
import com.demo.common.payload.Payload;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderState {

    @Id
    private UUID orderId;

    @Column(name = "status", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "order_created_payload", columnDefinition = "jsonb")
    private Payload orderCreatedPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_succeeded_payload", columnDefinition = "jsonb")
    private Payload paymentSucceededPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_failed_payload", columnDefinition = "jsonb")
    private Payload paymentFailedPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipment_arranged_payload", columnDefinition = "jsonb")
    private Payload shipmentArrangedPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "arrangement_failed_payload", columnDefinition = "jsonb")
    private Payload arrangementFailedPayload;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
