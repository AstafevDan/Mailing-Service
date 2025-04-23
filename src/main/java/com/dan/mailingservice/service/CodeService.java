package com.dan.mailingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Objects;

/**
 * Сервис для генерации, сохранения, валидации, удаления кода.
 * Использует Redis в качестве временного хранилища кодов.
 *
 * @author Даниил Астафьев
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeService {

    /**
     * Криптографически стойкий генератор случайных чисел.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Длина кода.
     */
    private static final Integer CODE_LENGTH = 6;

    /**
     * Символы, из которых может состоять код.
     */
    private static final String CHARACTERS = "0123456789";

    /**
     * Время хранения кода.
     */
    private static final Duration DURATION = Duration.ofMinutes(5);

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Генерирует и сохраняет в Redis код подтверждения. Результат выводит в консоль.
     *
     * @param email email пользователя
     */
    public void generateAndStoreCode(final String email) {
        final String code = generateCode(CHARACTERS, CODE_LENGTH);
        final String cacheKey = getCacheKey(email);

        redisTemplate.opsForValue().set(cacheKey, code, DURATION);
        log.info("Generated code for {} is {}", email, code);
    }

    /**
     * Проверяет валидность кода.
     *
     * @param email email пользователя
     * @param code  код подтверждения пользователя
     * @return true - если код валиден, иначе - false
     */
    public boolean isCodeValid(final String email, final String code) {
        final String cacheKey = getCacheKey(email);
        return Objects.equals(code, redisTemplate.opsForValue().get(cacheKey));
    }

    /**
     * Удаляет код из хранилища Redis.
     *
     * @param email email пользователя
     */
    public void deleteCode(final String email) {
        final String cacheKey = getCacheKey(email);
        redisTemplate.delete(cacheKey);
    }

    /**
     * Формирует ключ, под которым будет храниться код подтверждения в Redis.
     *
     * @param email email пользователя
     * @return ключ, под которым будет храниться код подтверждения в Redis.
     */
    private String getCacheKey(String email) {
        return "code:" + email;
    }

    /**
     * Непосредственно генерирует код подтверждения.
     *
     * @param characters символы, из которых может состоять код.
     * @param codeLength длина кода
     * @return код подтверждения
     */
    private String generateCode(String characters, Integer codeLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            int index = SECURE_RANDOM.nextInt(CHARACTERS.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }
}
