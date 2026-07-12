package com.chatop.api.message.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.chatop.api.rental.model.Rental;
import com.chatop.api.user.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageEmailService {

    private final Optional<JavaMailSender> mailSender;
    private final String from;

    public MessageEmailService(
        Optional<JavaMailSender> mailSender,
        @Value("${app.mail.from:no-reply@chatop.local}") String from
    ) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendRentalOwnerNotification(Rental rental, User sender, String messageContent) {
        if (rental.getOwner() == null || !StringUtils.hasText(rental.getOwner().getEmail())) {
            return;
        }

        mailSender.ifPresent(senderClient -> send(senderClient, rental, sender, messageContent));
    }

    private void send(JavaMailSender senderClient, Rental rental, User sender, String messageContent) {
        try {
            senderClient.send(mailMessage(rental, sender, messageContent));
        } catch (RuntimeException exception) {
            log.debug("Unable to send rental message email notification", exception);
        }
    }

    private SimpleMailMessage mailMessage(Rental rental, User sender, String messageContent) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        if (StringUtils.hasText(from)) {
            mailMessage.setFrom(from);
        }

        mailMessage.setTo(rental.getOwner().getEmail());
        mailMessage.setSubject("New message for your rental: " + rental.getName());
        mailMessage.setText("""
            Hello,

            %s (%s) sent you a message about your rental "%s":

            %s
            """.formatted(sender.getName(), sender.getEmail(), rental.getName(), messageContent));

        return mailMessage;
    }
}
