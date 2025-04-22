package com.dan.mailingservice.unit;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import com.dan.mailingservice.service.CodeService;
import com.dan.mailingservice.service.MailingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailingServiceTest {

    private static final String EMAIL = "test@test.com";
    private static final String CODE = "123456";

    @Mock
    private CodeService codeService;

    @InjectMocks
    private MailingService mailingService;

    @Test
    void sendCode() {
        mailingService.sendCode(EMAIL);

        verify(codeService).generateAndStoreCode(EMAIL);
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
