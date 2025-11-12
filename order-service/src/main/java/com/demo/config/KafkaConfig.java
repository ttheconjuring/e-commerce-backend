package com.demo.config;

import com.demo.common.Message;
import com.demo.common.constant.Topics;
import com.demo.exception.CancelOrderNonRetryableException;
import com.demo.exception.CancelOrderRetryableException;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.Map;

/**
 * Central Kafka configuration for the Order Service.
 * <p>
 * This class defines beans for:
 * <ol>
 * <li>Robust listener error handling with specific retry/non-retry exceptions.</li>
 * <li>The listener container factory to apply this error handler.</li>
 * <li>Automatic topic creation ({@link NewTopic}) for the topics this
 * service owns or listens to.</li>
 * </ol>
 */
@Configuration
public class KafkaConfig {

    /**
     * Configures the main error handler for all Kafka listeners.
     * <p>
     * This setup implements a retry mechanism with a Dead-Letter Topic (DLT) strategy.
     * <ul>
     * <li><b>Retries:</b> It will retry a failing message 3 times, with a fixed
     * 5-second (5000ms) delay between each attempt.</li>
     * <li><b>DLT:</b> After all retries are exhausted, the {@link DeadLetterPublishingRecoverer}
     * will automatically publish the problematic message to a DLT.</li>
     * <li><b>Specific Exceptions:</b>
     * <ul>
     * <li>{@link CancelOrderRetryableException} is explicitly marked as retryable.</li>
     * <li>{@link CancelOrderNonRetryableException} is explicitly marked as
     * non-retryable, meaning it will be sent *directly* to the DLT with no retries.</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param kafkaTemplate The {@link KafkaTemplate} used by the recoverer to
     * publish the poison pill message to the DLT.
     * @return A fully configured {@link DefaultErrorHandler}.
     */
    @Bean
    public DefaultErrorHandler errorHandler (KafkaTemplate<String, Message> kafkaTemplate) {
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        FixedBackOff fixedBackOff = new FixedBackOff(5000, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, fixedBackOff);
        errorHandler.addRetryableExceptions(CancelOrderRetryableException.class);
        errorHandler.addNotRetryableExceptions(CancelOrderNonRetryableException.class);
        return errorHandler;
    }

    /**
     * Configures the container factory for all {@link org.springframework.kafka.annotation.KafkaListener}
     * annotations.
     * <p>
     * This bean wires the custom {@link DefaultErrorHandler} (defined above)
     * into every listener in this application.
     *
     * @param consumerFactory The default Spring Boot consumer factory.
     * @param errorHandler    The custom, DLT-enabled error handler bean.
     * @return A configured {@link ConcurrentKafkaListenerContainerFactory}.
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Message>> kafkaListenerContainerFactory(
            ConsumerFactory<String, Message> consumerFactory, DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Message> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    /**
     * Defines and configures the {@link Topics#ORDER_EVENTS_TOPIC}.
     * <p>
     * This {@link Bean} will cause Spring to automatically create the topic
     * on the broker if it doesn't already exist.
     * <p>
     * Configuration:
     * <ul>
     * <li><b>Partitions:</b> 2 - Allows for concurrent consumption.</li>
     * <li><b>Replicas:</b> 3 - Standard for a production-ready, fault-tolerant setup.</li>
     * <li><b>Min In-Sync Replicas:</b> 2 - Guarantees that a message is written to at
     * least 2 replicas before being acknowledged. This prevents data loss
     * if the leader partition fails.</li>
     * </ul>
     *
     * @return A {@link NewTopic} definition.
     */
    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder
                .name(Topics.ORDER_EVENTS_TOPIC)
                .partitions(2)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    /**
     * Defines and configures the {@link Topics#ORDER_COMMANDS_TOPIC}.
     * <p>
     * This {@link Bean} will cause Spring to automatically create the topic
     * on the broker if it doesn't already exist.
     * <p>
     * Configuration:
     * <ul>
     * <li><b>Partitions:</b> 2 - Allows for concurrent consumption.</li>
     * <li><b>Replicas:</b> 3 - Standard for a production-ready, fault-tolerant setup.</li>
     * <li><b>Min In-Sync Replicas:</b> 2 - Guarantees that a message is written to at
     * least 2 replicas before being acknowledged. This prevents data loss
     * if the leader partition fails.</li>
     * </ul>
     *
     * @return A {@link NewTopic} definition.
     */
    @Bean
    public NewTopic orderCommandsTopic() {
        return TopicBuilder
                .name(Topics.ORDER_COMMANDS_TOPIC)
                .partitions(2)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

}
