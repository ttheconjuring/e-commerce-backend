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

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxEventService outboxEventService;
    private final OutboxEventRepository outboxEventRepository;
    private final PublisherService publisherService;

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