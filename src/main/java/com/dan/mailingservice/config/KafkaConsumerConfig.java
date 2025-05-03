package com.dan.mailingservice.config;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Класс конфигурации консюмера Kafka.
 *
 * @author Даниил Астафьев
 * @version 1.0
 */
@Configuration
public class KafkaConsumerConfig {

    /**
     * Бин консюмера Kafka.
     *
     * @param properties свойства консюмера Kafka
     * @return сконфигурированный {@link KafkaConsumer}
     */
    @Bean
    public KafkaConsumer<Long, String> kafkaConsumer(KafkaPropertiesConfig properties) {
        return new KafkaConsumer<>(properties.createConsumerProperties());
    }
}
