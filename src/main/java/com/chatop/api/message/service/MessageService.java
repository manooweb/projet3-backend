package com.chatop.api.message.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.dto.MessageResponse;
import com.chatop.api.message.model.Message;
import com.chatop.api.message.repository.MessageRepository;
import com.chatop.api.rental.model.Rental;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

@Service
public class MessageService {

    private static final String MESSAGE_SENT_MESSAGE = "Message send with success";

    private final MessageRepository messageRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final MessageEmailService messageEmailService;

    public MessageService(
        MessageRepository messageRepository,
        RentalRepository rentalRepository,
        UserRepository userRepository,
        MessageEmailService messageEmailService
    ) {
        this.messageRepository = messageRepository;
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.messageEmailService = messageEmailService;
    }

    @Transactional
    public MessageResponse create(CreateMessageRequest request, Authentication authentication) {
        Integer authenticatedUserId = userIdFromToken(authentication);

        if (!authenticatedUserId.equals(request.userId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user_id");
        }

        User sender = userRepository.findById(authenticatedUserId)
            .orElseThrow(this::unauthorized);
        Rental rental = rentalRepository.findById(request.rentalId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid rental_id"));

        String messageContent = request.message().trim();

        messageRepository.save(new Message(
            rental,
            sender,
            messageContent
        ));
        messageEmailService.sendRentalOwnerNotification(rental, sender, messageContent);

        return new MessageResponse(MESSAGE_SENT_MESSAGE);
    }

    private Integer userIdFromToken(Authentication authentication) {
        return Optional.ofNullable(authentication)
            .map(Authentication::getPrincipal)
            .filter(Jwt.class::isInstance)
            .map(Jwt.class::cast)
            .map(this::userIdFromToken)
            .orElseThrow(this::unauthorized);
    }

    private Integer userIdFromToken(Jwt jwt) {
        Object userIdClaim = jwt.getClaims().get("userId");

        if (userIdClaim instanceof Number number) {
            return number.intValue();
        }

        if (userIdClaim instanceof String userId) {
            try {
                return Integer.valueOf(userId);
            } catch (NumberFormatException exception) {
                throw unauthorized();
            }
        }

        throw unauthorized();
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
