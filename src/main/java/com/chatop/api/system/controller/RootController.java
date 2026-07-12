package com.chatop.api.system.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.SystemProperties;

@RestController
public class RootController {

    private final SystemProperties system;

    public RootController(ChatopProperties chatopProperties) {
        this.system = chatopProperties.getSystem();
    }

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
            "name", system.getName(),
            "status", system.getStatus(),
            "health", system.getHealthPath()
        );
    }
}
