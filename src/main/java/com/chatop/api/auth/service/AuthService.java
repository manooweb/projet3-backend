package com.chatop.api.auth.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.dto.AuthenticatedUserResponse;
import com.chatop.api.auth.dto.AuthTokenResponse;
import com.chatop.api.auth.dto.LoginRequest;
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

    @Transactional(readOnly = true)
    public AuthTokenResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw invalidCredentials();
        }

        return new AuthTokenResponse(jwtService.generateToken(user));
    }

    @Transactional(readOnly = true)
    public AuthenticatedUserResponse me(Authentication authentication) {
        Integer userId = Optional.ofNullable(authentication)
            .map(Authentication::getPrincipal)
            .filter(Jwt.class::isInstance)
            .map(Jwt.class::cast)
            .map(this::userIdFromToken)
            .orElseThrow(this::unauthorized);

        User user = userRepository.findById(userId)
            .orElseThrow(this::unauthorized);

        return new AuthenticatedUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
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

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
}
