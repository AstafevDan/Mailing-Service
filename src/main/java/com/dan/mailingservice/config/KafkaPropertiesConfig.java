package com.dan.mailingservice.config;

import com.sun.jdi.PrimitiveValue;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Класс конфигурации свойств Kafka.
 *
 * @author Даниил Астафьев
 * @version 1.0
 */
@Component
public class KafkaPropertiesConfig {

    /**
     * Сервер брокера Kafka.
     */
    @Value("${kafka.bootstrap-servers}")
    private String BROKER_URL;

    /**
     * Id группы консюмеров.
     */
    @Value("${kafka.consumer.group-id}")
    private String GROUP_ID;

    /**
     * Auto-offset reset конфигурация.
     */
    @Value("${kafka.consumer.auto-offset-reset}")
    private String AUTO_OFFSET_RESET;

    /**
     * Включен ли авто коммит оффсетов.
     */
    @Value("${kafka.consumer.enable-auto-commit}")
    private String ENABLE_AUTO_COMMIT;

    /**
     * Задает свойства консюмеру Kafka.
     *
     * @return свойства {@link Properties}
     */
    public Properties createConsumerProperties() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_URL);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_RESET);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, ENABLE_AUTO_COMMIT);
        return props;
    }

    /**
     * Задает свойства админа Kafka.
     *
     * @return свойства {@link Properties}
     */
    public Properties createAdminProperties() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER_URL);
        return props;
    }
}
