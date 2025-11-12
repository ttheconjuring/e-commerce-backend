package com.demo.service;

import java.util.UUID;

/**
 * Service interface for ensuring idempotent message processing.
 * <p>
 * In a distributed system, Kafka consumers (or any message consumer) may
 * receive the same message more than once. This service provides a mechanism
 * to track processed message IDs, allowing event handlers to safely check
 * if an event has already been handled, thus achieving "exactly-once" semantics
 * from a business logic perspective.
 *
 * @see com.demo.component.OrderEventsHandler
 * @see com.demo.component.PaymentEventsHandler
 */
public interface ConsumedMessageService {

    /**
     * Checks if a message with the given ID has already been processed.
     * <p>
     * This method should be atomic. It checks for the existence
     * of the ID and, if it doesn't exist, inserts it in a single transaction
     * or atomic operation.
     *
     * @param id The unique identifier (UUID) of the incoming event/message.
     * @return {@code true} if the ID has been seen before (it's a duplicate),
     * {@code false} if this is the first time seeing this ID.
     */
    boolean isDuplicate(UUID id);

    /**
     * A housekeeping method to periodically clean up old message IDs.
     * <p>
     * To prevent the consumed messages table/store from growing indefinitely,
     * this method can be called by a scheduler to delete records older than
     * a certain time (e.g., older than 30 days).
     */
    void cleanUp();

}
