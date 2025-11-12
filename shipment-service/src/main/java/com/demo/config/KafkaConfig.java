package com.demo.config;

import com.demo.common.Message;
import com.demo.common.constant.Topics;
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
 * Central Kafka configuration for the Shipment Service.
 * <p>
 * This class defines beans for:
 * 1. Robust listener error handling (Retry + DLT).
 * 2. The listener container factory to apply the error handler.
 * 3. Automatic topic creation ({@link NewTopic}) for payment topics.
 */
@Configuration
public class KafkaConfig {

    /**
     * Configures the main error handler for all Kafka listeners.
     * <p>
     * This setup implements a retry mechanism with a Dead-Letter Topic (DLT) strategy.</br>
     * 1. **Retries:** It will retry a failing message 3 times, with a fixed</br>
     * 5-second (5000ms) delay between each attempt. </br>
     * 2. **DLT:** After all retries are exhausted, the {@link DeadLetterPublishingRecoverer}
     * will automatically publish the problematic message to a DLT.</br>
     * 3. **Exceptions:** By default, all exceptions are considered retryable.
     * {@code errorHandler.addNotRetryableExceptions()} is used to specify
     * fatal exceptions that should *not* be retried (and go straight to DLT).
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
        errorHandler.addRetryableExceptions();
        errorHandler.addNotRetryableExceptions();
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
     * Defines and configures the {@link Topics#SHIPMENT_COMMANDS_TOPIC}.
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
    public NewTopic shippingCommandsTopic() {
        return TopicBuilder
                .name(Topics.SHIPMENT_COMMANDS_TOPIC)
                .partitions(2)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

    /**
     * Defines and configures the {@link Topics#SHIPMENT_EVENTS_TOPIC}.
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
    public NewTopic shippingEventsTopic() {
        return TopicBuilder
                .name(Topics.SHIPMENT_EVENTS_TOPIC)
                .partitions(2)
                .replicas(3)
                .configs(Map.of("min.insync.replicas", "2"))
                .build();
    }

}
