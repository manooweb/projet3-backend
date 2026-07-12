package com.chatop.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatop.responses")
public class ResponseMessagesProperties {

    private String rentalCreated;
    private String rentalUpdated;
    private String messageSent;
    private String authenticationSuccessful;
    private String logoutSuccessful;
}
