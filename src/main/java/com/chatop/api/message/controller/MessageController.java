package com.chatop.api.message.controller;

import static com.chatop.api.config.OpenApiConfig.COOKIE_AUTH_SECURITY_SCHEME;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.exception.dto.ApiErrorResponse;
import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.dto.MessageResponse;
import com.chatop.api.message.service.MessageService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes endpoints used to contact rental owners.
 */
@Tag(name = "Messages", description = "Rental owner contact endpoints")
@SecurityRequirement(name = COOKIE_AUTH_SECURITY_SCHEME)
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Sends a message from the authenticated user to a rental owner.
     *
     * @param request the message payload containing the rental, sender and message content
     * @param authentication the authenticated principal resolved by Spring Security
     * @return a success response once the message has been created
     */
    @Operation(summary = "Send message", description = "Sends a message from the authenticated user to a rental owner.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Message sent",
            content = @Content(schema = @Schema(implementation = MessageResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error, invalid user ID or invalid rental ID",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication is required",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "403",
            description = "CSRF token is missing or invalid",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
        )
    })
    @PostMapping
    public MessageResponse create(
        @Valid @RequestBody CreateMessageRequest request,
        @Parameter(hidden = true) Authentication authentication
    ) {
        return messageService.create(request, authentication);
    }
}
