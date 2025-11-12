package com.demo.model;

import com.demo.common.command.Command;
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
 * An entity representing a message to be published as part of the
 * Transactional Outbox pattern.
 * <p>
 * This entity is saved to the database in the same transaction as
 * the business logic (e.g., {@link OrderState} update). A separate
 * poller (e.g., {@link com.demo.component.OutboxPoller}) will read
 * this record, publish the {@link Command} to Kafka, and then
 * update its {@link Status}.
 */
@Entity
@Table(name = "outbox_commands")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OutboxCommand {

    /**
     * The unique identifier for this outbox record (the command id).
     */
    @Id
    private UUID id;

    /**
     * The name of the command, e.g., "PROCESS_PAYMENT".
     * Useful for logging and filtering.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The target Kafka topic where this command should be published.
     */
    @Column(nullable = false)
    private String topic;

    /**
     * The saga's correlation ID, used as the Kafka message key to
     * ensure partitioning.
     */
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    /**
     * The full command payload (the actual message to be sent).
     * Stored as JSON in the database.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Command command;

    /**
     * Timestamp of when this outbox record was created.
     */
    @Column(nullable = false)
    private Instant timestamp;

    /**
     * The publishing status of this command (e.g., PENDING_PUBLISHING,
     * PUBLISHED, PUBLISHING_FAILED).
     * Used by the poller to track its work.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

}
