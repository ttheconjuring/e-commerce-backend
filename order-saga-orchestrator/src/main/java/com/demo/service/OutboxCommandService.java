package com.demo.service;

import com.demo.common.command.Command;
import com.demo.model.OutboxCommand;
import com.demo.model.Status;

/**
 * Service interface for managing the Transactional Outbox.
 * <p>
 * This service is responsible for persisting commands (messages to be published)
 * into the database as part of an atomic transaction along with business state
 * changes (e.g., updating {@link com.demo.model.OrderState}).
 * <p>
 * A separate component (e.g., {@link com.demo.component.OutboxPoller})
 * is expected to read these commands, publish them to a message broker (Kafka),
 * and then update or delete them.
 */
public interface OutboxCommandService {

    /**
     * Creates and saves a new command to the outbox table.
     * <p>
     * This method is called by event handlers within the same transaction as the {@link OrderStateService} calls.
     *
     * @param command The command object to be saved.
     */
    void create(Command command);

    /**
     * Performs a bulk deletion of commands that have already been
     * successfully published (marked with {@link Status#PUBLISHED}).
     * <p>
     * This is a cleanup task, run periodically.
     */
    void deletePublished();

    /**
     * Updates an existing outbox command.
     * <p>
     * This is typically used by the poller component to mark a command's
     * status (e.g., from {@link Status#PENDING_PUBLISHING} to {@link Status#PUBLISHED})
     * after it has been successfully sent to the message broker.
     *
     * @param outboxCommand The command entity to update.
     * @param newStatus     The new status to set.
     */
    void update(OutboxCommand outboxCommand, Status newStatus);

}
