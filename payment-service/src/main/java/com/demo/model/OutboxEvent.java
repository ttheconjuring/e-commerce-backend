package com.demo.model;

import com.demo.common.event.Event;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * An entity representing an event to be published as part of the
 * Transactional Outbox pattern.
 * <p>
 * This entity is saved to the database in the same transaction as
 * the business logic (e.g., creating an {@link Payment}). A separate
 * poller (e.g., {@link com.demo.component.OutboxPoller}) will read
 * this record, publish the {@link Event} to Kafka, and then
 * update its {@link Status}.
 *
 * @see com.demo.service.OutboxEventService
 * @see com.demo.component.OutboxPoller
 */
@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    /**
     * The unique identifier for this event (the event id).
     * This ID is also used by consumers to ensure idempotent processing.
     */
    @Id
    private UUID id;

    /**
     * The name of the event, e.g., "ORDER_CREATED", "ORDER_CANCELLED".
     * Useful for logging and filtering.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The target Kafka topic where this event should be published.
     */
    @Column(nullable = false)
    private String topic;

    /**
     * The saga's correlation ID (which is the Order ID).
     * This is used as the Kafka message key to ensure partitioning.
     */
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    /**
     * The full event payload (the actual message to be sent).
     * Stored as JSON in the database.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Event event;

    /**
     * Timestamp of when this outbox record was created.
     */
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * The publishing status of this event (e.g., PENDING_PUBLISHING,
     * PUBLISHED, PUBLISHING_FAILED).
     * Used by the poller to track its work.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

}
