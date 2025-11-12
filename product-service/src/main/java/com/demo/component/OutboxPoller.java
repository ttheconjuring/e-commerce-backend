package com.demo.component;

import com.demo.common.event.Event;
import com.demo.model.OutboxEvent;
import com.demo.model.Status;
import com.demo.repository.OutboxEventRepository;
import com.demo.service.OutboxEventService;
import com.demo.service.PublisherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implements the "Polling Publisher" part of the Transactional Outbox pattern.
 * <p>
 * This component periodically scans the outbox table for events that are
 * pending publication ({@link Status#PENDING_PUBLISHING}). It then attempts
 * to publish each event to the message broker (Kafka) using the
 * {@link PublisherService}.
 * <p>
 * The publishing is done asynchronously. Based on the outcome, it updates
 * the event's status to {@link Status#PUBLISHED} on success or
 * {@link Status#PUBLISHING_FAILED} on failure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventService outboxEventService;
    private final OutboxEventRepository outboxEventRepository;
    private final PublisherService publisherService;

    /**
     * A scheduled task that runs at a fixed delay to process pending outbox events.
     * <p>
     * It fetches all events marked as {@link Status#PENDING_PUBLISHING},
     * attempts to publish them, and updates their status based on the
     * asynchronous publishing result.
     */
    @Scheduled(fixedDelay = 10000) // 10s
    public void pollOutbox() {
        List<OutboxEvent> outboxEvents = this.outboxEventRepository.findByStatus(Status.PENDING_PUBLISHING);
        for (OutboxEvent outboxEvent : outboxEvents) {
            String topic = outboxEvent.getTopic();
            String correlationId = String.valueOf(outboxEvent.getCorrelationId());
            Event event = outboxEvent.getEvent();

            // Asynchronously publish the message
            this.publisherService.publish(topic, correlationId, event)
                    .thenRun(() -> {
                        // Success: Mark as published
                        this.outboxEventService.update(outboxEvent, Status.PUBLISHED);
                    })
                    .exceptionally(ex -> {
                        // Failure: Mark as failed so it can be retried or investigated
                        this.outboxEventService.update(outboxEvent, Status.PUBLISHING_FAILED);
                        return null;
                    });
        }
    }

}