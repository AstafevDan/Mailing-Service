package com.dan.mailingservice.integration.service;

import com.dan.mailingservice.integration.IntegrationTestBase;
import com.dan.mailingservice.service.CodeService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@RequiredArgsConstructor
public class CodeServiceIT extends IntegrationTestBase {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";
    private static final String CACHE_KEY = "code:" + EMAIL;
    private static final Duration DURATION = Duration.ofMinutes(5);

    private final CodeService codeService;
    private final RedisTemplate<String, String> redisTemplate;

    @Test
    void generateAndStoreCode_Success() {
        codeService.generateAndStoreCode(EMAIL);

        String storedCode = redisTemplate.opsForValue().get(CACHE_KEY);
        assertNotNull(storedCode);
        assertThat(storedCode.length()).isEqualTo(CODE.length());
        assertTrue(storedCode.matches("^[0-9]{6}$"));

        Long ttl = redisTemplate.getExpire(CACHE_KEY);
        assertNotNull(ttl);
        assertTrue(ttl > 0 && ttl <= DURATION.getSeconds());
    }

    @Test
    void isCodeValid_CodeIsValid() {
        codeService.generateAndStoreCode(EMAIL);
        String storedCode = redisTemplate.opsForValue().get(CACHE_KEY);

        assertNotNull(storedCode);
        assertTrue(codeService.isCodeValid(EMAIL, storedCode));
    }

    @Test
    void isCodeValid_CodeIsNotValid() {
        codeService.generateAndStoreCode(EMAIL);
        String storedCode = redisTemplate.opsForValue().get(CACHE_KEY);

        assertNotNull(storedCode);
        assertFalse(codeService.isCodeValid(EMAIL, CODE));
    }

    @Test
    void isCodeValid_CodeDoesNotExist() {
        assertFalse(codeService.isCodeValid(EMAIL, CODE));
    }

    @Test
    void deleteCode_Success() {
        codeService.generateAndStoreCode(EMAIL);
        assertNotNull(redisTemplate.opsForValue().get(CACHE_KEY));

        codeService.deleteCode(EMAIL);

        assertNull(redisTemplate.opsForValue().get(CACHE_KEY));
    }
}
