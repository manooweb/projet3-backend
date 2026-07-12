package com.chatop.api.message.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.service.CurrentUserService;
import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ErrorMessagesProperties;
import com.chatop.api.config.properties.ResponseMessagesProperties;
import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.dto.MessageResponse;
import com.chatop.api.message.model.Message;
import com.chatop.api.message.repository.MessageRepository;
import com.chatop.api.rental.model.Rental;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.user.model.User;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final RentalRepository rentalRepository;
    private final MessageEmailService messageEmailService;
    private final CurrentUserService currentUserService;
    private final ResponseMessagesProperties responses;
    private final ErrorMessagesProperties errors;

    public MessageService(
        MessageRepository messageRepository,
        RentalRepository rentalRepository,
        MessageEmailService messageEmailService,
        CurrentUserService currentUserService,
        ChatopProperties chatopProperties
    ) {
        this.messageRepository = messageRepository;
        this.rentalRepository = rentalRepository;
        this.messageEmailService = messageEmailService;
        this.currentUserService = currentUserService;
        this.responses = chatopProperties.getResponses();
        this.errors = chatopProperties.getErrors();
    }

    @Transactional
    public MessageResponse create(CreateMessageRequest request, Authentication authentication) {
        Integer authenticatedUserId = currentUserService.getUserId(authentication);

        if (!authenticatedUserId.equals(request.userId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getInvalidUserId());
        }

        User sender = currentUserService.getUser(authenticatedUserId);
        Rental rental = rentalRepository.findById(request.rentalId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.getInvalidRentalId()));

        String messageContent = request.message().trim();

        messageRepository.save(new Message(
            rental,
            sender,
            messageContent
        ));
        messageEmailService.sendRentalOwnerNotification(rental, sender, messageContent);

        return new MessageResponse(responses.getMessageSent());
    }
}
