package com.chatop.api.config;

import java.nio.file.Path;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.UploadsProperties;

@Configuration
public class StaticResourceConfig {

    @Bean
    WebMvcConfigurer rentalUploadsResourceConfigurer(ChatopProperties chatopProperties) {
        UploadsProperties uploads = chatopProperties.getUploads();
        Path rentalUploadsPath = Path.of(uploads.getRentalsDir()).toAbsolutePath().normalize();
        String rentalUploadsUrlPathPattern = uploads.getRentalsUrlPathPattern();

        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry
                    .addResourceHandler(rentalUploadsUrlPathPattern)
                    .addResourceLocations(resourceLocation(rentalUploadsPath));
            }
        };
    }

    private String resourceLocation(Path rentalUploadsPath) {
        String location = rentalUploadsPath.toUri().toString();

        return location.endsWith("/") ? location : location + "/";
    }
}
