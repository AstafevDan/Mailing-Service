package com.dan.mailingservice.integration.service;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import com.dan.mailingservice.integration.IntegrationTestBase;
import com.dan.mailingservice.service.CodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@AutoConfigureMockMvc
public class MailingControllerIT extends IntegrationTestBase {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";
    private static final String CACHE_KEY = "code:" + EMAIL;

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final CodeService codeService;
    private final RedisTemplate<String, String> redisTemplate;

    @Test
    void verifyCode_Success() throws Exception {
        codeService.generateAndStoreCode(EMAIL);
        String storedCode = redisTemplate.opsForValue().get(CACHE_KEY);

        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(storedCode)
                .build();

        mockMvc.perform(post("/api/v1/codes/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void verifyCode_Failure() throws Exception {
        codeService.generateAndStoreCode(EMAIL);

        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(CODE)
                .build();

        mockMvc.perform(post("/api/v1/codes/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
