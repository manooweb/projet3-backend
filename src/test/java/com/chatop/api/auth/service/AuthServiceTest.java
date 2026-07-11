package com.chatop.api.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.dto.AuthenticatedUserResponse;
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

    @Test
    void meReturnsAuthenticatedUserFromJwtUserId() {
        User user = new User("test@example.com", "Test", "encoded-password");
        ReflectionTestUtils.setField(user, "id", 1);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        AuthenticatedUserResponse response = authService.me(authenticationWithUserId(1));

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Test");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    void meThrowsUnauthorizedWhenJwtIsMissing() {
        assertThatThrownBy(() -> authService.me(null))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meThrowsUnauthorizedWhenUserDoesNotExist() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.me(authenticationWithUserId(1)))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.UNAUTHORIZED);
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
