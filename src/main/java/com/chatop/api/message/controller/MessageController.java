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

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public MessageResponse create(
        @Valid @RequestBody CreateMessageRequest request,
        Authentication authentication
    ) {
        return messageService.create(request, authentication);
    }
}
