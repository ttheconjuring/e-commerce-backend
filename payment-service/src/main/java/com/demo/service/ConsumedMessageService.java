package com.demo.service;

import com.demo.model.ConsumedMessage;
import com.demo.repository.ConsumedMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumedMessageService {

    private final ConsumedMessageRepository consumedMessageRepository;

    @Transactional
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

    @Scheduled(fixedRate = 240000) // 4 min
    // @Scheduled(cron = "0 0 3 * * 0") // 03:00 Every Sunday
    public void cleanUp() {
        this.consumedMessageRepository.deleteAll();
    }

}
