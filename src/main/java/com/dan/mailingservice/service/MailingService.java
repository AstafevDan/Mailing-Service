package com.dan.mailingservice.service;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * Название топика Kafka.
     */
    private static final String TOPIC_NAME = "users";

    /**
     * Запущен ли консюмер Kafka.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final CountDownLatch latch = new CountDownLatch(1);
    private final CodeService codeService;
    private final KafkaConsumer<Long, String> kafkaConsumer;

    /**
     * Подписка консюмера на топик Kafka.
     */
    @PostConstruct
    public void init() {
        kafkaConsumer.subscribe(List.of(TOPIC_NAME));
        sendCode();
    }

    /**
     * Слушает топик Kafka, получает email пользователя и печатает код подтверждения для него в консоль.
     */
    @Async
    public void sendCode() {
        try {
            while (!running.get()) {
                ConsumerRecords<Long, String> records = kafkaConsumer.poll(Duration.ofSeconds(5));

                if (!records.isEmpty()) {
                    processRecords(records);
                    commitOffsets();
                }
            }
        } catch (WakeupException e) {
            // ignore, we're closing
        } catch (Exception e) {
            log.error("Unexpected error", e);
        } finally {
            kafkaConsumer.close();
            latch.countDown();
        }
    }

    /**
     * Синхронно коммитит оффсеты.
     */
    private void commitOffsets() {
        try {
            kafkaConsumer.commitSync();
        } catch (CommitFailedException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Обрабатывает все полученные записи.
     *
     * @param records записи, полученные из топика Kafka.
     */
    private void processRecords(ConsumerRecords<Long, String> records) {
        for (ConsumerRecord<Long, String> record : records) {
            try {
                processSingleRecord(record);
            } catch (Exception e) {
                log.error("Error processing record: {}", record, e);
            }
        }
    }

    /**
     * Обрабатывает одну запись из топика, генерируя код для указанного email.
     *
     * @param record запись, полученная из топика Kafka.
     */
    private void processSingleRecord(ConsumerRecord<Long, String> record) {
        String email = record.value();
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

    /**
     * Корректное завершение работы консюмера.
     *
     * @throws InterruptedException выбрасываемое исключение
     */
    @PreDestroy
    public void shutdown() throws InterruptedException {
        running.set(true);
        latch.await();
        kafkaConsumer.wakeup();
    }
}
