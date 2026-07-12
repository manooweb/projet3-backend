package com.chatop.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatop.open-api")
public class OpenApiProperties {

    private String title;
    private String description;
    private String version;
}
