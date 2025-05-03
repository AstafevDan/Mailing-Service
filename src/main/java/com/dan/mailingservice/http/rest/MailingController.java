package com.dan.mailingservice.http.rest;

import com.dan.mailingservice.dto.CodeVerificationRequest;
import com.dan.mailingservice.dto.CodeVerificationResponse;
import com.dan.mailingservice.service.MailingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер сервиса рассылки кодов для управления верификацией пользователя.
 *
 * @author Даниил Астафьев
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/codes")
@RequiredArgsConstructor
@Tag(name = "Mailing Controller", description = "Контроллер сервиса рассылки кодов для управления верификацией пользователя")
public class MailingController {

    private final MailingService mailingService;

    /**
     * Позволяет верифицировать email пользователя с помощью проверки кода подтверждения.
     *
     * @param codeVerificationRequest {@link CodeVerificationRequest} запрос на верификацию пользователя
     * @return {@link ResponseEntity}, содержащий {@link CodeVerificationResponse} с ответом о том, валиден ли код подтверждения
     */
    @Operation(
            summary = "Верификация email пользователя",
            description = "Позволяет верифицировать email пользователя с помощью проверки кода подтверждения",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Код валиден и пользователь может быть верифицирован")
            }
    )
    @PostMapping("/verify")
    public ResponseEntity<CodeVerificationResponse> verifyCode(
            @RequestBody @Valid CodeVerificationRequest codeVerificationRequest
    ) {
        boolean isValid = mailingService.verifyEmail(codeVerificationRequest);
        return ResponseEntity.ok(new CodeVerificationResponse(isValid));
    }
}
