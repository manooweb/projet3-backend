package com.chatop.api.user.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ErrorMessagesProperties;
import com.chatop.api.user.dto.UserResponse;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ErrorMessagesProperties errors;

    public UserService(UserRepository userRepository, ChatopProperties chatopProperties) {
        this.userRepository = userRepository;
        this.errors = chatopProperties.getErrors();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Integer id) {
        return userRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, errors.getUserNotFound()));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
