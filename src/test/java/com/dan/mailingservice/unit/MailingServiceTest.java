package com.dan.mailingservice.unit;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import com.dan.mailingservice.service.CodeService;
import com.dan.mailingservice.service.MailingService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailingServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";

    @Mock
    private CodeService codeService;

    @Mock
    private KafkaConsumer<Long, String> consumer;

    @InjectMocks
    private MailingService mailingService;

    @Test
    void sendCode() {
        ConsumerRecord<Long, String> record = new ConsumerRecord<>("users", 0, 0L, 1L, EMAIL);
        ConsumerRecords<Long, String> records = new ConsumerRecords<>(
                Map.of(new TopicPartition("users", 0), Collections.singletonList(record))
        );

        AtomicInteger pollCounter = new AtomicInteger(0);

        when(consumer.poll(any(Duration.class))).thenAnswer(inv -> {
            if (pollCounter.getAndIncrement() == 0) {
                return records;
            }
            mailingService.shutdown();
            return ConsumerRecords.empty();
        });

        mailingService.sendCode();

        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(codeService).generateAndStoreCode(EMAIL);
            verify(consumer).commitSync();
        });
    }

    @Test
    void verifyEmail_CodeIsValid() {
        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(CODE)
                .build();
        when(codeService.isCodeValid(EMAIL, CODE)).thenReturn(true);

        assertTrue(mailingService.verifyEmail(request));
        verify(codeService).isCodeValid(EMAIL, CODE);
        verify(codeService).deleteCode(EMAIL);
    }

    @Test
    void verifyEmail_CodeIsInvalid() {
        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(CODE)
                .build();
        when(codeService.isCodeValid(EMAIL, CODE)).thenReturn(false);

        assertFalse(mailingService.verifyEmail(request));
        verify(codeService).isCodeValid(EMAIL, CODE);
        verify(codeService, never()).deleteCode(EMAIL);
    }
}
