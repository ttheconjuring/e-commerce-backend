package com.demo.service;

import com.demo.common.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublisherService {

    private final KafkaTemplate<String, Message> kafkaTemplate;

    public CompletableFuture<SendResult<String, Message>> publish(String topic, String correlationId, Message message) {
        // Create the Kafka record with topic, key, and payload
        ProducerRecord<String, Message> record = new ProducerRecord<>(topic, correlationId, message);
        // Send the message asynchronously
        CompletableFuture<SendResult<String, Message>> future = this.kafkaTemplate.send(record);
        // Attach a callback for logging the result of the send operation
        future.whenComplete((result, exception) -> {
            if (exception == null) {
                // On success
                log.info("---> {} was published to {} <---", message.getClass().getSimpleName(), topic);
            } else {
                // On failure
                log.error("---> Failed to publish {} to {} <---", message.getClass().getSimpleName(), topic, exception);
            }
        });
        // Return the future immediately to the caller (e.g., the OutboxPoller)
        return future;
    }

}
