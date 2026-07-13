package com.chatop.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatop.open-api.demo-user")
public class SwaggerDemoUserProperties {

    private String name;
    private String email;
    private String password;
}
