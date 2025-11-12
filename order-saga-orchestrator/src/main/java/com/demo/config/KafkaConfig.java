package com.demo.config;

import com.demo.common.Message;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

/**
 * Central Kafka configuration for the Order Saga Orchestrator.
 * <p>
 * This class defines the beans responsible for configuring the Kafka
 * listeners, with a special focus on robust error handling using
 * a retry mechanism and a Dead-Letter Topic (DLT) strategy.
 */
@Configuration
public class KafkaConfig {

    /**
     * Configures the main error handler for all Kafka listeners.
     * <p>
     * This setup implements a retry mechanism with a Dead-Letter Topic (DLT) strategy.</br>
     * 1. **Retries:** It will retry a failing message 3 times, with a fixed
     * 5-second (5000ms) delay between each attempt.</br>
     * 2. **DLT:** After all retries are exhausted, the {@link DeadLetterPublishingRecoverer}
     * will automatically publish the problematic message to a Dead-Letter Topic
     * (DLT) for later inspection and manual intervention. The DLT topic name
     * is typically the original topic name with a "-dlt" suffix.
     * <p>
     * This approach is critical for resiliency, as it prevents a single
     * "poison pill" (a malformed or problematic message) from blocking
     * the consumer and halting all further processing on that partition.
     *
     * @param kafkaTemplate The {@link KafkaTemplate} used by the recoverer to
     * publish the poison pill message to the DLT.
     * @return A fully configured {@link DefaultErrorHandler}.
     */
    @Bean
    public DefaultErrorHandler errorHandler (KafkaTemplate<String, Message> kafkaTemplate) {
        // Configure 3 retries with a 5s fixed back-off
        FixedBackOff fixedBackOff = new FixedBackOff(5000, 3);
        // Configure the DLT publisher
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        // Create the error handler
        return new DefaultErrorHandler(deadLetterPublishingRecoverer, fixedBackOff);
    }

    /**
     * Configures the container factory for all {@link org.springframework.kafka.annotation.KafkaListener}
     * annotations.
     * <p>
     * This bean is responsible for creating the listener containers that
     * manage the consumer threads. Its most important job here is to
     * wire the custom {@link DefaultErrorHandler} (defined above) into
     * every listener in the application.
     *
     * @param consumerFactory The default Spring Boot consumer factory.
     * @param errorHandler    The custom, DLT-enabled error handler from the
     * {@link #errorHandler(KafkaTemplate)} bean.
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

}
