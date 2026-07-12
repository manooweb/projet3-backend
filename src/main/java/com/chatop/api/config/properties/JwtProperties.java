package com.chatop.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatop.jwt")
public class JwtProperties {

    private String secret;
    private long expirationSeconds;
}
