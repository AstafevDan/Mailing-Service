package com.dan.mailingservice.unit;

import com.dan.mailingservice.service.CodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CodeServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";
    private static final String CACHE_KEY = "code:" + EMAIL;
    private static final Duration DURATION = Duration.ofMinutes(5);

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private CodeService codeService;

    @Test
    void generateAndStoreCode_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doNothing().when(valueOperations).set(anyString(), anyString(), eq(DURATION));

        codeService.generateAndStoreCode(EMAIL);

        verify(redisTemplate).opsForValue();
        verify(valueOperations, times(1)).set(anyString(), anyString(), eq(DURATION));
    }

    @Test
    void isCodeValid_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(CODE);

        assertTrue(codeService.isCodeValid(EMAIL, CODE));

        verify(redisTemplate).opsForValue();
        verify(valueOperations, times(1)).get(CACHE_KEY);
    }

    @Test
    void isCodeValid_CodeDoesNotMatch() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(CODE);

        assertFalse(codeService.isCodeValid(EMAIL, "111111"));

        verify(redisTemplate).opsForValue();
        verify(valueOperations, times(1)).get(CACHE_KEY);
    }

    @Test
    void isCodeValid_CodeIsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);

        assertFalse(codeService.isCodeValid(EMAIL, CODE));

        verify(redisTemplate).opsForValue();
        verify(valueOperations, times(1)).get(CACHE_KEY);
    }

    @Test
    void deleteCode_Success() {
        when(redisTemplate.delete(CACHE_KEY)).thenReturn(true);

        codeService.deleteCode(EMAIL);

        verify(redisTemplate).delete(CACHE_KEY);
    }
}
