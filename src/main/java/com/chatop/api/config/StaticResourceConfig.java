package com.chatop.api.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final Path rentalUploadsPath;

    public StaticResourceConfig(@Value("${app.uploads.rentals-dir}") String rentalUploadsDir) {
        this.rentalUploadsPath = Path.of(rentalUploadsDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/api/uploads/rentals/**")
            .addResourceLocations(resourceLocation());
    }

    private String resourceLocation() {
        String location = rentalUploadsPath.toUri().toString();

        return location.endsWith("/") ? location : location + "/";
    }
}
