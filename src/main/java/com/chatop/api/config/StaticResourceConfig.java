package com.chatop.api.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.UploadsProperties;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path rentalUploadsPath;
    private final String rentalUploadsUrlPathPattern;

    public StaticResourceConfig(ChatopProperties chatopProperties) {
        UploadsProperties uploads = chatopProperties.getUploads();

        this.rentalUploadsPath = Path.of(uploads.getRentalsDir()).toAbsolutePath().normalize();
        this.rentalUploadsUrlPathPattern = uploads.getRentalsUrlPathPattern();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler(rentalUploadsUrlPathPattern)
            .addResourceLocations(resourceLocation());
    }

    private String resourceLocation() {
        String location = rentalUploadsPath.toUri().toString();

        return location.endsWith("/") ? location : location + "/";
    }
}
