package com.chatop.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.dto.AuthTokenResponse;
import com.chatop.api.auth.dto.LoginRequest;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void loginReturnsTokenWhenCredentialsAreValid() {
        User user = new User("test@example.com", "Test", "encoded-password");

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthTokenResponse response = authService.login(
            new LoginRequest(" Test@Example.com ", "password")
        );

        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void loginThrowsUnauthorizedWhenEmailDoesNotExist() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("missing@example.com", "password")
            ))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(passwordEncoder, never()).matches("password", "encoded-password");
    }

    @Test
    void loginThrowsUnauthorizedWhenPasswordDoesNotMatch() {
        User user = new User("test@example.com", "Test", "encoded-password");

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("test@example.com", "wrong-password")
            ))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(jwtService, never()).generateToken(user);
    }
}
