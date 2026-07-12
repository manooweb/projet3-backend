package com.chatop.api.message.service;

import java.util.Optional;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.RentalMessageMailProperties;
import com.chatop.api.rental.model.Rental;
import com.chatop.api.user.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MessageEmailService {

    private final Optional<JavaMailSender> mailSender;
    private final String from;
    private final RentalMessageMailProperties rentalMessageMail;

    public MessageEmailService(
        Optional<JavaMailSender> mailSender,
        ChatopProperties chatopProperties
    ) {
        this.mailSender = mailSender;
        this.from = chatopProperties.getMail().getFrom();
        this.rentalMessageMail = chatopProperties.getRentalMessageMail();
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
            log.debug(rentalMessageMail.getDeliveryFailureLog(), exception);
        }
    }

    private SimpleMailMessage mailMessage(Rental rental, User sender, String messageContent) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        if (StringUtils.hasText(from)) {
            mailMessage.setFrom(from);
        }

        mailMessage.setTo(rental.getOwner().getEmail());
        mailMessage.setSubject(rentalMessageMail.getSubjectTemplate().formatted(rental.getName()));
        mailMessage.setText(rentalMessageMail.getBodyTemplate()
            .formatted(sender.getName(), sender.getEmail(), rental.getName(), messageContent));

        return mailMessage;
    }
}
