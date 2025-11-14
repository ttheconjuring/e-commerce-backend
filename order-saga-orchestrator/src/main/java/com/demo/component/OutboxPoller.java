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

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxCommandService outboxCommandService;
    private final OutboxCommandRepository outboxCommandRepository;
    private final PublisherService publisherService;

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
