package com.dan.mailingservice.integration.service;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import com.dan.mailingservice.integration.IntegrationTestBase;
import com.dan.mailingservice.service.CodeService;
import com.dan.mailingservice.service.MailingService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class MailingServiceIT extends IntegrationTestBase {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";
    private static final String CACHE_KEY = "code:" + EMAIL;

    private final MailingService mailingService;
    private final CodeService codeService;
    private final RedisTemplate<String, String> redisTemplate;

    @Test
    void sendCode() throws InterruptedException, ExecutionException {
        try (KafkaProducer<Long, String> producer = createProducer()) {
            final ProducerRecord<Long, String> record = new ProducerRecord<>("users", 1L, EMAIL);
            producer.send(record).get();
        }

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            String storedCode = redisTemplate.opsForValue().get(CACHE_KEY);
            assertNotNull(storedCode);
            assertEquals(CODE.length(), storedCode.length());
        });
    }

    @Test
    void verifyEmail_CodeIsValid() {
        codeService.generateAndStoreCode(EMAIL);
        String storedCode = redisTemplate.opsForValue().get(CACHE_KEY);
        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(storedCode)
                .build();

        assertTrue(mailingService.verifyEmail(request));
        assertNull(redisTemplate.opsForValue().get(CACHE_KEY));
    }

    @Test
    void verifyEmail_CodeIsInvalid() {
        codeService.generateAndStoreCode(EMAIL);
        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(CODE)
                .build();

        assertFalse(mailingService.verifyEmail(request));
        assertNotNull(redisTemplate.opsForValue().get(CACHE_KEY));
    }

    @Test
    void verifyCode_CodeDoesNotExist() {
        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(CODE)
                .build();

        assertFalse(mailingService.verifyEmail(request));
    }

    private KafkaProducer<Long, String> createProducer() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}
