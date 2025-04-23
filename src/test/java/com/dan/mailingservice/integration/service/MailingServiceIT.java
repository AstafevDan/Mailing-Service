package com.dan.mailingservice.integration.service;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import com.dan.mailingservice.integration.IntegrationTestBase;
import com.dan.mailingservice.service.CodeService;
import com.dan.mailingservice.service.MailingService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class MailingServiceIT extends IntegrationTestBase {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";
    private static final String CACHE_KEY = "code:" + EMAIL;

    private final MailingService mailingService;
    private final CodeService codeService;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<Long, String> kafkaTemplate;

    @Test
    void sendCode() throws InterruptedException {
        kafkaTemplate.send("users", EMAIL);

        TimeUnit.MILLISECONDS.sleep(1000);

        String storedCode = redisTemplate.opsForValue().get(CACHE_KEY);
        assertNotNull(storedCode);
        assertEquals(CODE.length(), storedCode.length());
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
}
