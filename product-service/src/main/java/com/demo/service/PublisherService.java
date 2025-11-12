package com.demo.service;

import com.demo.common.Message;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

/**
 * Service interface for publishing messages to the message broker (Kafka).
 * <p>
 * This interface abstracts the underlying message publishing mechanism,
 * allowing components like the {@link com.demo.component.OutboxPoller}
 * to send messages without being coupled to a specific KafkaTemplate.
 */
public interface PublisherService {

    /**
     * Asynchronously publishes a message to a specific Kafka topic.
     * <p>
     * The method returns a {@link CompletableFuture} which allows the caller
     * (e.g., the OutboxPoller) to chain actions to be executed upon
     * successful publication or failure.
     *
     * @param topic         The Kafka topic to which the message will be sent.
     * @param correlationId The key for the Kafka message, typically the saga's
     * correlation ID, ensuring messages for the same
     * saga land in the same partition (if configured).
     * @param message       The message (command/event) payload to be sent.
     * @return A {@link CompletableFuture} holding the {@link SendResult} of the
     * asynchronous send operation.
     */
    CompletableFuture<SendResult<String, Message>> publish(String topic, String correlationId, Message message);

}
