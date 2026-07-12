package com.chatop.api.system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.SystemProperties;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes system health information.
 */
@Tag(name = "System", description = "System status endpoints")
@RestController
@RequestMapping("/api")
public class HealthController {

    private final SystemProperties system;

    public HealthController(ChatopProperties chatopProperties) {
        this.system = chatopProperties.getSystem();
    }

    /**
     * Returns the API health status.
     *
     * @return the current health status
     */
    @Operation(summary = "Get API health status")
    @ApiResponse(
        responseCode = "200",
        description = "API health status",
        content = @Content(schema = @Schema(implementation = HealthResponse.class))
    )
    @GetMapping("/health")
    public HealthResponse health() {
        return new HealthResponse(system.getHealthStatus());
    }

    public record HealthResponse(String status) {
    }
}
