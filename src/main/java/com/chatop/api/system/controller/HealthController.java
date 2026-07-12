package com.chatop.api.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.SystemProperties;

@RestController
@RequestMapping("/api")
public class HealthController {

    private final SystemProperties system;

    public HealthController(ChatopProperties chatopProperties) {
        this.system = chatopProperties.getSystem();
    }

    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(system.getHealthStatus());
    }

    public record HealthResponse(String status) {
    }
}
