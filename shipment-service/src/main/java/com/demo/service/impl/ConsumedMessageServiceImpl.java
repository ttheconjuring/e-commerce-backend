package com.demo.service.impl;

import com.demo.model.ConsumedMessage;
import com.demo.repository.ConsumedMessageRepository;
import com.demo.service.ConsumedMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Concrete implementation of the {@link ConsumedMessageService} interface.
 * <p>
 * This class implements idempotent message processing using a database-centric
 * "try-to-insert" strategy. It leverages the database's unique key constraint
 * on the {@link ConsumedMessage} entity's ID.
 * <p>
 * This approach is highly concurrent and atomic, as it relies on the
 * database to guarantee uniqueness rather than a separate "check-then-act"
 * operation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumedMessageServiceImpl implements ConsumedMessageService {

    private final ConsumedMessageRepository consumedMessageRepository;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation attempts to {@code saveAndFlush} a new
     * {@link ConsumedMessage} with the given ID.
     * <ul>
     * <li><b>If the save succeeds:</b> The ID is new. The method returns {@code false} (not a duplicate).</li>
     * <li><b>If the save fails with a {@link DataIntegrityViolationException}:</b>
     * The ID already exists in the database (violating the unique/primary key
     * constraint). The method catches this exception, logs a warning, and
     * returns {@code true} (it is a duplicate).</li>
     * </ul>
     * This operation is {@link @Transactional} to ensure the insert (or its
     * failure) is committed immediately.
     */
    @Transactional
    @Override
    public boolean isDuplicate(UUID id) {
        try {
            // Attempt to save the new message ID.
            this.consumedMessageRepository.saveAndFlush(new ConsumedMessage(id, Instant.now()));
            // No exception = new message
            return false;
        } catch (DataIntegrityViolationException e) {
            // Exception = primary key violation = duplicate
            log.warn("---> Skipping duplicate message: {} <---", id);
            return true;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is a scheduled task that runs at a fixed rate
     * (e.g., every 4 minutes) to clear the consumed messages table.
     * <p>
     */
    @Override
    @Scheduled(fixedRate = 240000) // 4 min
    // @Scheduled(cron = "0 0 3 * * 0") // 03:00 Every Sunday
    public void cleanUp() {
        this.consumedMessageRepository.deleteAll();
    }

}

