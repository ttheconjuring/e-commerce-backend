package com.demo.model;

import com.demo.common.payload.Payload;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * An immutable entity that records the history of state changes for an {@link OrderState}.
 * <p>
 * This class acts as an **audit log** for the saga. A new entry is created
 * every time a significant event is processed, capturing the saga's
 * {@link Status} and the event {@link Payload} at that specific moment.
 * <p>
 * All fields are marked as {@code updatable = false} to ensure a
 * tamper-proof historical record, which is invaluable for debugging
 * and business intelligence.
 *
 * @see OrderState
 * @see com.demo.service.OrderStateService
 */
@Entity
@Table(name = "orders_state_history")
@Getter
@NoArgsConstructor
public class OrderStateHistory {

    /**
     * The unique primary key for this specific history record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The ID of the {@link OrderState} this history entry belongs to.
     * This is the "correlation ID" for the saga.
     */
    @Column(name = "order_id",  nullable = false, updatable = false)
    private UUID orderId;

    /**
     * The {@link Status} of the order *at the time this entry was recorded*.
     */
    @Column(name = "status", length = 50, nullable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    /**
     * The event payload  that
     * *triggered* this state or action. Stored as JSON for full audibility.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", updatable = false)
    private Payload payload;

    /**
     * The exact timestamp of when this history entry was created.
     */
    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    /**
     * Constructs a new history entry from the current state of an order
     * and the payload that triggered the change.
     *
     * @param orderState The {@link OrderState} object, from which the
     * orderId and current status will be copied.
     * @param payload    The event payload that triggered this new state.
     */
    public OrderStateHistory(OrderState orderState, Payload payload) {
        this.orderId = orderState.getOrderId();
        this.status = orderState.getStatus();
        this.payload = payload;
        this.timestamp = Instant.now();
    }

}
