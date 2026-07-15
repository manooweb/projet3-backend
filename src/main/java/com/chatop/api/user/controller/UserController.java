package com.chatop.api.user.controller;

import static com.chatop.api.config.OpenApiConfig.COOKIE_AUTH_SECURITY_SCHEME;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.exception.dto.ApiErrorResponse;
import com.chatop.api.user.dto.UserResponse;
import com.chatop.api.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes user lookup endpoints.
 */
@Tag(name = "Users", description = "User lookup endpoints")
@SecurityRequirement(name = COOKIE_AUTH_SECURITY_SCHEME)
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns a user's public profile.
     *
     * @param id the user identifier
     * @return the user's public information
     */
    @Operation(summary = "Get user", description = "Returns a user's public profile.")
    @ApiResponse(
        responseCode = "200",
        description = "User found",
        content = @Content(schema = @Schema(implementation = UserResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Unexpected server error",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}")
    public UserResponse findById(@Parameter(description = "User ID") @PathVariable Integer id) {
        return userService.findById(id);
    }
}
