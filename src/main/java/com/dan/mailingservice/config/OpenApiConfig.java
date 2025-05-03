package com.dan.mailingservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

/**
 * Класс конфигурации OpenAPI спецификации.
 *
 * @author Даниил Астафьев
 * @version 1.0
 */
@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Даниил"
                ),
                title = "Mailing Service API",
                description = "OpenApi документация для сервиса рассылки кодов от Даниила Астафьева",
                version = "1.0.0"
        ),
        servers = {
                @Server(
                        url = "http://localhost:8089",
                        description = "Локальное окружение"
                )
        }
)
public class OpenApiConfig {
}
