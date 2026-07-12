package com.chatop.api.message.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.chatop.api.rental.model.Rental;
import com.chatop.api.user.model.User;

class MessageEmailServiceTest {

    @Test
    void sendRentalOwnerNotificationSendsEmailToRentalOwner() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MessageEmailService messageEmailService = new MessageEmailService(
            Optional.of(mailSender),
            "no-reply@chatop.local"
        );

        messageEmailService.sendRentalOwnerNotification(
            rentalWithOwnerEmail("owner@example.com"),
            userWithId(2, "sender@example.com"),
            "Hello"
        );

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendRentalOwnerNotificationDoesNothingWhenMailSenderIsMissing() {
        MessageEmailService messageEmailService = new MessageEmailService(
            Optional.empty(),
            "no-reply@chatop.local"
        );

        assertThatCode(() -> messageEmailService.sendRentalOwnerNotification(
            rentalWithOwnerEmail("owner@example.com"),
            userWithId(2, "sender@example.com"),
            "Hello"
        )).doesNotThrowAnyException();
    }

    @Test
    void sendRentalOwnerNotificationDoesNotPropagateMailErrors() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MessageEmailService messageEmailService = new MessageEmailService(
            Optional.of(mailSender),
            "no-reply@chatop.local"
        );

        org.mockito.Mockito.doThrow(new MailSendException("SMTP unavailable"))
            .when(mailSender)
            .send(any(SimpleMailMessage.class));

        assertThatCode(() -> messageEmailService.sendRentalOwnerNotification(
            rentalWithOwnerEmail("owner@example.com"),
            userWithId(2, "sender@example.com"),
            "Hello"
        )).doesNotThrowAnyException();
    }

    @Test
    void sendRentalOwnerNotificationSkipsBlankOwnerEmail() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MessageEmailService messageEmailService = new MessageEmailService(
            Optional.of(mailSender),
            "no-reply@chatop.local"
        );

        messageEmailService.sendRentalOwnerNotification(
            rentalWithOwnerEmail(" "),
            userWithId(2, "sender@example.com"),
            "Hello"
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    private Rental rentalWithOwnerEmail(String ownerEmail) {
        Rental rental = new Rental(
            "House",
            120L,
            950L,
            "http://localhost:9001/api/uploads/rentals/house.jpg",
            "A nice house",
            userWithId(1, ownerEmail)
        );
        ReflectionTestUtils.setField(rental, "id", 1);

        return rental;
    }

    private User userWithId(Integer id, String email) {
        User user = new User(email, "Test", "encoded-password");
        ReflectionTestUtils.setField(user, "id", id);

        return user;
    }
}
