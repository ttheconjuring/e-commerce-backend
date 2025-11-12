package com.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * An entity for tracking processed message IDs to ensure idempotent consumption.
 * <p>
 * When an event handler
 * receives a message, it first checks if its ID exists in this table. If it
 * does, the message is a duplicate and is ignored. If it doesn't, the ID is
 * inserted, and the business logic proceeds.
 *
 * @see com.demo.service.ConsumedMessageService
 */
@Entity
@Table(name = "consumed_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsumedMessage {

    /**
     * The unique identifier (UUID) of the consumed Kafka message.
     * This comes directly from the message's header or payload.
     */
    @Id
    @Column(unique = true, nullable = false)
    private UUID id;

    /**
     * The timestamp of when this message was first processed.
     * Useful for cleanup jobs (e.g., deleting records older than 30 days).
     */
    @Column(nullable = false)
    private Instant timestamp;

}
