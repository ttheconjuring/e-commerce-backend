package com.demo.component;

import com.demo.common.command.Command;

import com.demo.model.OutboxCommand;
import com.demo.model.Status;
import com.demo.repository.OutboxCommandRepository;
import com.demo.service.OutboxCommandService;
import com.demo.service.PublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implements the "Polling Publisher" part of the Transactional Outbox pattern.
 * <p>
 * This component periodically scans the outbox table for commands that are
 * pending publication ({@link Status#PENDING_PUBLISHING}). It then attempts
 * to publish each command to the message broker (Kafka) using the
 * {@link PublisherService}.
 * <p>
 * The publishing is done asynchronously. Based on the outcome, it updates
 * the command's status to {@link Status#PUBLISHED} on success or
 * {@link Status#PUBLISHING_FAILED} on failure.
 *
 * @see com.demo.service.OutboxCommandService
 * @see com.demo.service.PublisherService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxCommandService outboxCommandService;
    private final OutboxCommandRepository outboxCommandRepository;
    private final PublisherService publisherService;

    /**
     * A scheduled task that runs at a fixed delay to process pending outbox commands.
     * <p>
     * It fetches all commands marked as {@link Status#PENDING_PUBLISHING},
     * attempts to publish them, and updates their status based on the
     * asynchronous publishing result.
     */
    @Scheduled(fixedDelay = 10000) // 10 sec
    public void pollOutbox() {
        List<OutboxCommand> outboxCommands = this.outboxCommandRepository.findByStatus(Status.PENDING_PUBLISHING);
        for (OutboxCommand outboxCommand : outboxCommands) {
            String topic = outboxCommand.getTopic();
            String correlationId = String.valueOf(outboxCommand.getCorrelationId());
            Command command = outboxCommand.getCommand();

            // Asynchronously publish the message
            this.publisherService.publish(topic, correlationId, command)
                    .thenRun(() -> {
                        // Success: Mark as published
                        this.outboxCommandService.update(outboxCommand, Status.PUBLISHED);
                    })
                    .exceptionally(ex -> {
                        // Failure: Mark as failed so it can be retried or investigated
                        this.outboxCommandService.update(outboxCommand, Status.PUBLISHING_FAILED);
                        return null;
                    });
        }
    }

}
