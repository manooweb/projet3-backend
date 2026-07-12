package com.chatop.api.user.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.user.dto.UserResponse;
import com.chatop.api.user.service.UserService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void findByIdReturnsUser() throws Exception {
        when(userService.findById(1))
            .thenReturn(new UserResponse(
                1,
                "Test User",
                "test@example.com",
                LocalDateTime.of(2026, 7, 12, 10, 30),
                LocalDateTime.of(2026, 7, 12, 10, 45)
            ));

        mockMvc.perform(get("/api/user/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Test User")))
            .andExpect(jsonPath("$.email", is("test@example.com")))
            .andExpect(jsonPath("$.created_at", is("2026-07-12T10:30:00")))
            .andExpect(jsonPath("$.updated_at", is("2026-07-12T10:45:00")));

        verify(userService).findById(1);
    }

    @Test
    void findByIdReturnsNotFoundWhenUserDoesNotExist() throws Exception {
        when(userService.findById(99))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/api/user/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status", is(404)))
            .andExpect(jsonPath("$.error", is("Not Found")))
            .andExpect(jsonPath("$.message", is("User not found")))
            .andExpect(jsonPath("$.path", is("/api/user/99")));
    }

    @Test
    void findByIdReturnsInternalServerErrorWhenUnexpectedErrorOccurs() throws Exception {
        when(userService.findById(1))
            .thenThrow(new IllegalStateException("Database connection lost"));

        mockMvc.perform(get("/api/user/1"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status", is(500)))
            .andExpect(jsonPath("$.error", is("Internal Server Error")))
            .andExpect(jsonPath("$.message", is("Internal Server Error")))
            .andExpect(jsonPath("$.path", is("/api/user/1")));
    }
}
