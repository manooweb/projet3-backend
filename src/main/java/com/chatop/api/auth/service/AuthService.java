package com.chatop.api.auth.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.dto.AuthTokenResponse;
import com.chatop.api.auth.dto.RegisterRequest;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthTokenResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        User user = new User(
            email,
            request.name().trim(),
            passwordEncoder.encode(request.password())
        );

        try {
            User savedUser = userRepository.saveAndFlush(user);

            return new AuthTokenResponse(jwtService.generateToken(savedUser));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
