package com.demo.service.impl;

import com.demo.common.constant.Topics;
import com.demo.common.event.Event;
import com.demo.model.OutboxEvent;
import com.demo.model.Status;
import com.demo.repository.OutboxEventRepository;
import com.demo.service.OutboxEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Concrete implementation of the {@link OutboxEventService} interface.
 * <p>
 * This service implements the Transactional Outbox pattern for publishing
 * **events**. Its primary responsibility is to take a generic {@link Event}
 * object (e.g., from {@link PaymentServiceImpl} or
 * {@link com.demo.component.PaymentCommandsHandler}) and persist it as an
 * {@link OutboxEvent} entity in the database.
 * <p>
 * This operation is designed to be called *within* the same database
 * transaction as the business logic, ensuring that an event is only saved
 * if the business state change (e.g., creating an Order) is also successful.
 *
 * @see com.demo.repository.OutboxEventRepository
 * @see com.demo.component.OutboxPoller
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation builds a new {@link OutboxEvent} entity from the
     * provided {@link Event}.
     * <p>
     * It sets the initial status to {@link Status#PENDING_PUBLISHING} so
     * that the {@link com.demo.component.OutboxPoller} can pick it up.
     * Unlike the command outbox, this implementation routes all events
     * to a single topic: {@link Topics#ORDER_EVENTS_TOPIC}.
     * <p>
     * This method is {@link @Transactional} and flushes the save immediately.
     */
    @Transactional
    @Override
    public void create(Event event) {
        OutboxEvent outboxEvent = new OutboxEvent();
        // 1. Copy common event properties
        outboxEvent.setId(event.getId());
        outboxEvent.setName(event.getName());
        // 2. Set the destination topic (all events go to the same topic)
        outboxEvent.setTopic(Topics.PAYMENT_EVENTS_TOPIC);
        outboxEvent.setCorrelationId(event.getCorrelationId());
        outboxEvent.setEvent(event);
        outboxEvent.setTimestamp(event.getTimestamp());
        outboxEvent.setStatus(Status.PENDING_PUBLISHING);
        // 3. Save to database atomically
        this.outboxEventRepository.saveAndFlush(outboxEvent);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This is a scheduled housekeeping task that runs periodically (every 2 minutes)
     * to clean up the outbox table.
     * <p>
     * It finds all events that have been successfully
     * marked as {@link Status#PUBLISHED} and deletes them in a
     * single bulk operation to prevent the table from growing indefinitely.
     */
    @Transactional
    @Scheduled(fixedRate = 120000) // 2 min
    // @Scheduled(cron = "0 0 3 * * 0") // 03:00 Every Sunday
    @Override
    public void deletePublished() {
        this.outboxEventRepository.deleteAll(this.outboxEventRepository.findByStatus(Status.PUBLISHED));
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method is called by the {@link com.demo.component.OutboxPoller}
     * to update the status of an event after a publishing attempt.
     * It is {@link @Transactional} to ensure the status update is
     * committed immediately.
     *
     * @param outboxEvent The *managed* entity to update.
     * @param newStatus   The new status (e.g., {@link Status#PUBLISHED} or
     * {@link Status#PUBLISHING_FAILED}).
     */
    @Transactional
    @Override
    public void update(OutboxEvent outboxEvent, Status newStatus) {
        outboxEvent.setStatus(newStatus);
        this.outboxEventRepository.saveAndFlush(outboxEvent);
    }

}
