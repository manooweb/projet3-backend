package com.chatop.api.config.properties;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class ChatopProperties {

    private final JwtProperties jwt;
    private final UploadsProperties uploads;
    private final MailProperties mail;
    private final RentalMessageMailProperties rentalMessageMail;
    private final OpenApiProperties openApi;
    private final SwaggerDemoUserProperties swaggerDemoUser;
    private final SystemProperties system;
    private final ResponseMessagesProperties responses;
    private final ErrorMessagesProperties errors;
}
