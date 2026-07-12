package com.chatop.api.message.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.dto.MessageResponse;
import com.chatop.api.message.service.MessageService;

import jakarta.validation.Valid;

/**
 * Exposes endpoints used to contact rental owners.
 */
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
    @PostMapping
    public MessageResponse create(
        @Valid @RequestBody CreateMessageRequest request,
        Authentication authentication
    ) {
        return messageService.create(request, authentication);
    }
}
