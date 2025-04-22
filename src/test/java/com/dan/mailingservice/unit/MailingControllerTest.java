package com.dan.mailingservice.unit;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import com.dan.mailingservice.http.rest.MailingController;
import com.dan.mailingservice.service.MailingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = MailingController.class)
public class MailingControllerTest {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private MailingService mailingService;

    @Test
    void verifyCode_Success() throws Exception {
        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(CODE)
                .build();
        when(mailingService.verifyEmail(any(CodeVerificationRequest.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/codes/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void verifyCode_Failure() throws Exception {
        CodeVerificationRequest request = CodeVerificationRequest.builder()
                .email(EMAIL)
                .code(CODE)
                .build();
        when(mailingService.verifyEmail(any(CodeVerificationRequest.class))).thenReturn(false);

        mockMvc.perform(post("/api/v1/codes/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
