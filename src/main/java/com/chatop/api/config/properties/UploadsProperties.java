package com.chatop.api.config.properties;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "chatop.uploads")
public class UploadsProperties {

    private String rentalsDir;
    private String rentalsUrlPath;
    private List<String> allowedPictureExtensions;

    public String getRentalsUrlPathWithTrailingSlash() {
        return rentalsUrlPath.endsWith("/") ? rentalsUrlPath : rentalsUrlPath + "/";
    }

    public String getRentalsUrlPathPattern() {
        return getRentalsUrlPathWithTrailingSlash() + "**";
    }
}
