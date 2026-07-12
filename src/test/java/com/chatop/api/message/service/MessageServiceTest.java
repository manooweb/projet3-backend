package com.chatop.api.message.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.service.CurrentUserService;
import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ChatopPropertiesTestFactory;
import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.dto.MessageResponse;
import com.chatop.api.message.model.Message;
import com.chatop.api.message.repository.MessageRepository;
import com.chatop.api.rental.model.Rental;
import com.chatop.api.rental.repository.RentalRepository;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

class MessageServiceTest {

    private MessageRepository messageRepository;
    private RentalRepository rentalRepository;
    private UserRepository userRepository;
    private MessageEmailService messageEmailService;
    private CurrentUserService currentUserService;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageRepository = mock(MessageRepository.class);
        rentalRepository = mock(RentalRepository.class);
        userRepository = mock(UserRepository.class);
        messageEmailService = mock(MessageEmailService.class);
        ChatopProperties chatopProperties = ChatopPropertiesTestFactory.defaultProperties();
        currentUserService = new CurrentUserService(userRepository, chatopProperties);
        messageService = new MessageService(
            messageRepository,
            rentalRepository,
            messageEmailService,
            currentUserService,
            chatopProperties
        );
    }

    @Test
    void createSavesMessageForAuthenticatedUserAndRental() {
        User sender = userWithId(2);
        Rental rental = rentalWithId(1);

        when(userRepository.findById(2)).thenReturn(Optional.of(sender));
        when(rentalRepository.findById(1)).thenReturn(Optional.of(rental));
        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = messageService.create(
            new CreateMessageRequest(1, 2, " Hello "),
            authenticationWithUserId(2)
        );

        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();

        assertThat(response.message()).isEqualTo("Message send with success");
        assertThat(savedMessage.getRental()).isSameAs(rental);
        assertThat(savedMessage.getUser()).isSameAs(sender);
        assertThat(savedMessage.getContent()).isEqualTo("Hello");
        verify(messageEmailService).sendRentalOwnerNotification(rental, sender, "Hello");
    }

    @Test
    void createThrowsBadRequestWhenRequestUserDoesNotMatchAuthenticatedUser() {
        assertThatThrownBy(() -> messageService.create(
                new CreateMessageRequest(1, 3, "Hello"),
                authenticationWithUserId(2)
            ))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(messageRepository, never()).save(any(Message.class));
        verify(userRepository, never()).findById(any());
        verify(rentalRepository, never()).findById(any());
        verify(messageEmailService, never()).sendRentalOwnerNotification(any(), any(), any());
    }

    @Test
    void createThrowsBadRequestWhenRentalDoesNotExist() {
        when(userRepository.findById(2)).thenReturn(Optional.of(userWithId(2)));
        when(rentalRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(
                new CreateMessageRequest(1, 2, "Hello"),
                authenticationWithUserId(2)
            ))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(messageRepository, never()).save(any(Message.class));
        verify(messageEmailService, never()).sendRentalOwnerNotification(any(), any(), any());
    }

    @Test
    void createThrowsUnauthorizedWhenAuthenticatedUserDoesNotExist() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> messageService.create(
                new CreateMessageRequest(1, 2, "Hello"),
                authenticationWithUserId(2)
            ))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(messageRepository, never()).save(any(Message.class));
        verify(rentalRepository, never()).findById(any());
        verify(messageEmailService, never()).sendRentalOwnerNotification(any(), any(), any());
    }

    private User userWithId(Integer id) {
        User user = new User("test@example.com", "Test", "encoded-password");
        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }

    private Rental rentalWithId(Integer id) {
        Rental rental = new Rental(
            "House",
            120L,
            950L,
            "http://localhost:9001/api/uploads/rentals/house.jpg",
            "A nice house",
            userWithId(1)
        );
        ReflectionTestUtils.setField(rental, "id", id);

        return rental;
    }

    private JwtAuthenticationToken authenticationWithUserId(Integer userId) {
        return new JwtAuthenticationToken(jwtWithUserId(userId));
    }

    private Jwt jwtWithUserId(Integer userId) {
        Instant now = Instant.now();

        return new Jwt(
            "jwt-token",
            now,
            now.plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of("sub", "test@example.com", "userId", userId)
        );
    }
}
