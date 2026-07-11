package com.chatop.api.auth.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.dto.AuthTokenResponse;
import com.chatop.api.auth.dto.LoginRequest;
import com.chatop.api.auth.dto.RegisterRequest;
import com.chatop.api.auth.service.AuthService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerReturnsGeneratedToken() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
            .thenReturn(new AuthTokenResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Test",
                      "email": "test@example.com",
                      "password": "password"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is("jwt-token")));
    }

    @Test
    void registerReturnsBadRequestWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "",
                      "email": "invalid-email",
                      "password": ""
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsGeneratedToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenReturn(new AuthTokenResponse("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "test@example.com",
                      "password": "password"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token", is("jwt-token")));
    }

    @Test
    void loginReturnsBadRequestWhenBodyIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "invalid-email",
                      "password": ""
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsUnauthorizedWhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "test@example.com",
                      "password": "wrong-password"
                    }
                    """))
            .andExpect(status().isUnauthorized());
    }
}
