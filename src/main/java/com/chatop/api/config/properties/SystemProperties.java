package com.chatop.api.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatop.system")
public class SystemProperties {

    private String name;
    private String version;
    private String status;
    private String healthPath;
    private String healthStatus;
}
