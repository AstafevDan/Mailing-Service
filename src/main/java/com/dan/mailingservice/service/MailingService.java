package com.dan.mailingservice.service;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Сервис, занимающийся рассылкой кодов подтверждения (выводом их в консоль) и верификацией email пользователей.
 *
 * @author Даниил Астафьев
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailingService {

    private final CodeService codeService;

    /**
     * Слушает топик Kafka, получает email пользователя и печатает код подтверждения для него в консоль.
     *
     * @param email email пользователя
     */
    @KafkaListener(topics = "users", groupId = "users-group")
    public void sendCode(String email) {
        log.info("Received message from {}", email);
        codeService.generateAndStoreCode(email);
    }

    /**
     * Верифицирует email пользователя, проверяя валидность его кода подтверждения.
     *
     * @param request {@link CodeVerificationRequest} запрос на верификацию пользователя
     * @return true - если код валиден, иначе - false
     */
    public boolean verifyEmail(CodeVerificationRequest request) {
        if (!codeService.isCodeValid(request.getEmail(), request.getCode())) {
            log.error("Invalid code for email {}", request.getEmail());
            return false;
        }
        codeService.deleteCode(request.getEmail());
        return true;
    }
}
