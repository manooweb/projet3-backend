package com.chatop.api.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.user.dto.UserResponse;
import com.chatop.api.user.model.User;
import com.chatop.api.user.repository.UserRepository;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void findByIdReturnsMappedUser() {
        User user = new User("test@example.com", "Test User", "encoded-password");
        ReflectionTestUtils.setField(user, "id", 1);
        ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.of(2026, 7, 12, 10, 30));
        ReflectionTestUtils.setField(user, "updatedAt", LocalDateTime.of(2026, 7, 12, 10, 45));

        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1);

        assertThat(response.id()).isEqualTo(1);
        assertThat(response.name()).isEqualTo("Test User");
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.createdAt()).isEqualTo(LocalDateTime.of(2026, 7, 12, 10, 30));
        assertThat(response.updatedAt()).isEqualTo(LocalDateTime.of(2026, 7, 12, 10, 45));
    }

    @Test
    void findByIdThrowsNotFoundWhenUserDoesNotExist() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(1))
            .isInstanceOf(ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
