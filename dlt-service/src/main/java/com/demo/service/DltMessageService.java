package com.demo.service;

import com.demo.common.Message;
import com.demo.model.DltMessage;
import com.demo.model.DltMessageDTO;
import com.demo.model.Status;
import com.demo.repository.DltMessagesRepository;
import com.demo.utility.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DltMessageService {

    private final DltMessagesRepository dltMessagesRepository;

    @Transactional
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

    public List<DltMessageDTO> retrieveAll() {
        return this.dltMessagesRepository.findAll()
                .stream()
                .map(Utils::covertToDto) // Map Entity -> DTO
                .toList();
    }

}
