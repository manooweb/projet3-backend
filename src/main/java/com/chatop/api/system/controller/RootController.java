package com.chatop.api.system.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping("/")
    public Map<String, String> root() {
        return Map.of(
            "name", "Châtop API",
            "status", "running",
            "health", "/api/health"
        );
    }
}
