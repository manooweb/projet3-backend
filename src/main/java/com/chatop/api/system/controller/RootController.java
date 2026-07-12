package com.chatop.api.system.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.SystemProperties;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Exposes a lightweight API entry point.
 */
@Hidden
@RestController
public class RootController {

    private final SystemProperties system;

    public RootController(ChatopProperties chatopProperties) {
        this.system = chatopProperties.getSystem();
    }

    /**
     * Returns basic API discovery information.
     *
     * @return the API name, status and health endpoint path
     */
    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
            "name", system.getName(),
            "status", system.getStatus(),
            "health", system.getHealthPath()
        );
    }
}
