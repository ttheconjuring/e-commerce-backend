package com.demo.service;

import com.demo.common.event.Event;
import com.demo.model.OutboxEvent;
import com.demo.model.Status;

/**
 * Service interface for managing the Transactional Outbox.
 * <p>
 * This service is responsible for persisting events (messages to be published)
 * into the database as part of an atomic transaction along with business state
 * changes.
 * <p>
 * A separate component (e.g., {@link com.demo.component.OutboxPoller})
 * is expected to read these events, publish them to a message broker (Kafka),
 * and then update or delete them.
 */
public interface OutboxEventService {

    /**
     * Creates and saves a new event to the outbox table.
     * <p>
     * This method is called by event handlers within the same transaction as the {@link ShipmentService} calls.
     *
     * @param event The event object to be saved.
     */
    void create(Event event);

    /**
     * Performs a bulk deletion of events that have already been
     * successfully published (marked with {@link Status#PUBLISHED}).
     * <p>
     * This is a cleanup task, run periodically.
     */
    void deletePublished();

    /**
     * Updates an existing outbox event.
     * <p>
     * This is typically used by the poller component to mark a event's
     * status (e.g., from {@link Status#PENDING_PUBLISHING} to {@link Status#PUBLISHED})
     * after it has been successfully sent to the message broker.
     *
     * @param outboxEvent   The event entity to update.
     * @param newStatus     The new status to set.
     */
    void update(OutboxEvent outboxEvent, Status newStatus);

}