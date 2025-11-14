package com.demo.service;

import com.demo.common.command.Command;
import com.demo.common.command.order.CancelOrderCommand;
import com.demo.common.command.order.CompleteOrderCommand;
import com.demo.common.command.payment.ProcessPaymentCommand;
import com.demo.common.command.product.ConfirmAvailabilityCommand;
import com.demo.common.command.product.UpdateProductsCommand;
import com.demo.common.constant.Topics;
import com.demo.model.OutboxCommand;
import com.demo.model.Status;
import com.demo.repository.OutboxCommandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxCommandService {

    private final OutboxCommandRepository outboxCommandRepository;

    @Transactional
    public void create(Command command) {
        OutboxCommand outboxCommand = new OutboxCommand();
        // 1. Copy common command properties
        outboxCommand.setId(command.getId());
        outboxCommand.setName(command.getName());
        outboxCommand.setCorrelationId(command.getCorrelationId());
        outboxCommand.setCommand(command); // The full JSON payload
        outboxCommand.setTimestamp(command.getTimestamp());
        outboxCommand.setStatus(Status.PENDING_PUBLISHING);
        // 2. Perform Topic Routing based on command type
        if (command instanceof CancelOrderCommand ||
                command instanceof CompleteOrderCommand) {
            outboxCommand.setTopic(Topics.ORDER_COMMANDS_TOPIC);
        } else if (command instanceof ProcessPaymentCommand) {
            outboxCommand.setTopic(Topics.PAYMENT_COMMANDS_TOPIC);
        } else if (command instanceof ConfirmAvailabilityCommand ||
                command instanceof UpdateProductsCommand) {
            outboxCommand.setTopic(Topics.PRODUCT_COMMANDS_TOPIC);
        } else {
            outboxCommand.setTopic(Topics.SHIPMENT_COMMANDS_TOPIC);
        }
        // 3. Save to database atomically
        this.outboxCommandRepository.saveAndFlush(outboxCommand);
    }

    @Transactional
    @Scheduled(fixedRate = 120000) // 2 min
    // @Scheduled(cron = "0 0 3 * * 0") // 03:00 Every Sunday
    public void deletePublished() {
        this.outboxCommandRepository.deleteAll(this.outboxCommandRepository.findByStatus(Status.PUBLISHED));
    }

    @Transactional
    public void update(OutboxCommand outboxCommand, Status newStatus) {
        outboxCommand.setStatus(newStatus);
        this.outboxCommandRepository.saveAndFlush(outboxCommand);
    }

}
