package com.demo.service;

import com.demo.common.constant.Topics;
import com.demo.common.event.Event;
import com.demo.model.OutboxEvent;
import com.demo.model.Status;
import com.demo.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    public void create(Event event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        // 1. Copy common event properties
        outboxEvent.setId(event.getId());
        outboxEvent.setName(event.getName());
        // 2. Set the destination topic (all events go to the same topic)
        outboxEvent.setTopic(Topics.SHIPMENT_EVENTS_TOPIC);
        outboxEvent.setCorrelationId(event.getCorrelationId());
        outboxEvent.setEvent(event);
        outboxEvent.setTimestamp(event.getTimestamp());
        outboxEvent.setStatus(Status.PENDING_PUBLISHING);
        // 3. Save to database atomically
        this.outboxEventRepository.saveAndFlush(outboxEvent);
    }

    @Transactional
    @Scheduled(fixedRate = 120000) // 2 min
    // @Scheduled(cron = "0 0 3 * * 0") // 03:00 Every Sunday
    public void deletePublished() {
        this.outboxEventRepository.deleteAll(this.outboxEventRepository.findByStatus(Status.PUBLISHED));
    }

    @Transactional
    public void update(OutboxEvent outboxEvent, Status newStatus) {
        outboxEvent.setStatus(newStatus);
        this.outboxEventRepository.saveAndFlush(outboxEvent);
    }

}
