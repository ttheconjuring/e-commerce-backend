package com.demo.service.impl;

import com.demo.common.Message;
import com.demo.model.DltMessage;
import com.demo.model.DltMessageDTO;
import com.demo.model.Status;
import com.demo.repository.DltMessagesRepository;
import com.demo.service.DltMessageService;
import com.demo.utility.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Concrete implementation of the {@link DltMessageService} interface.
 * <p>
 * This class handles the business logic of mapping incoming DLT
 * {@link Message} objects to {@link DltMessage} entities and persisting
 * them. It also handles retrieving and converting these entities
 * into DTOs for the API layer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DltMessageServiceImpl implements DltMessageService {

    private final DltMessagesRepository dltMessagesRepository;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation is {@link @Transactional} to ensure the save
     * operation is atomic. It builds a new {@link DltMessage} entity
     * from the raw failed message, mapping its core properties.
     * <p>
     * It sets the initial status to {@link Status#UNRESOLVED} and uses
     * {@link Utils#determineDescription(String)} to create a
     * human-readable summary before saving it to the database.
     */
    @Transactional
    @Override
    public void register(Message message) {
        // 1. Create the new DLT entity
        DltMessage dltMessage = new DltMessage();

        // 2. Map fields from the failed message
        dltMessage.setMessageId(message.getId());
        dltMessage.setCorrelationId(message.getCorrelationId());
        dltMessage.setType(message.getType().name() + ": " + message.getName());
        dltMessage.setPayload(message.getPayload());

        // 3. Set initial metadata
        dltMessage.setStatus(Status.UNRESOLVED);
        dltMessage.setDescription(Utils.determineDescription(message.getClass().getSimpleName()));
        dltMessage.setReceivedAt(Instant.now());
        dltMessage.setUpdatedAt(Instant.now());

        // 4. Save to DB and log
        this.dltMessagesRepository.saveAndFlush(dltMessage);
        log.info("---> {} was registered <---", message.getClass().getSimpleName());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation fetches all {@link DltMessage} entities from the
     * repository, then uses a stream to map each entity to a
     * {@link DltMessageDTO} via the {@link Utils#covertToDto(DltMessage)}
     * helper method for consumption by the API.
     */
    @Override
    public List<DltMessageDTO> retrieveAll() {
        return this.dltMessagesRepository.findAll()
                .stream()
                .map(Utils::covertToDto) // Map Entity -> DTO
                .toList();
    }

}
