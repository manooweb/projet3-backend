package com.chatop.api.auth.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import com.chatop.api.auth.dto.AuthenticatedUserResponse;
import com.chatop.api.auth.dto.LoginRequest;
import com.chatop.api.auth.dto.RegisterRequest;
import com.chatop.api.auth.security.JwtCookieService;
import com.chatop.api.auth.service.AuthService;
import com.chatop.api.config.properties.ResponseMessagesProperties;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtCookieService jwtCookieService;

    @MockitoBean
    private ResponseMessagesProperties responses;

    @BeforeEach
    void setUp() {
        when(responses.getAuthenticationSuccessful()).thenReturn("Authentication successful");
        when(responses.getLogoutSuccessful()).thenReturn("Logout successful");
    }

    @Test
    void registerSetsAuthenticationCookieWithoutReturningToken() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
            .thenReturn("jwt-token");
        when(jwtCookieService.createAuthenticationCookie("jwt-token"))
            .thenReturn(authenticationCookie("jwt-token"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Test",
                      "email": "test@example.com",
                      "password": "password"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string(HttpHeaders.SET_COOKIE, containsString("CHATOP_AUTH=jwt-token")))
            .andExpect(jsonPath("$.message", is("Authentication successful")))
            .andExpect(jsonPath("$.token").doesNotExist());
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
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Bad Request")))
            .andExpect(jsonPath("$.message", is("Validation failed")))
            .andExpect(jsonPath("$.path", is("/api/auth/register")))
            .andExpect(jsonPath("$.field_errors[0].field", is("email")))
            .andExpect(jsonPath("$.field_errors[1].field", is("name")))
            .andExpect(jsonPath("$.field_errors[2].field", is("password")));
    }

    @Test
    void loginSetsAuthenticationCookieWithoutReturningToken() throws Exception {
        when(authService.login(any(LoginRequest.class)))
            .thenReturn("jwt-token");
        when(jwtCookieService.createAuthenticationCookie("jwt-token"))
            .thenReturn(authenticationCookie("jwt-token"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "test@example.com",
                      "password": "password"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string(HttpHeaders.SET_COOKIE, containsString("CHATOP_AUTH=jwt-token")))
            .andExpect(jsonPath("$.message", is("Authentication successful")))
            .andExpect(jsonPath("$.token").doesNotExist());
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
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Bad Request")))
            .andExpect(jsonPath("$.message", is("Validation failed")))
            .andExpect(jsonPath("$.path", is("/api/auth/login")))
            .andExpect(jsonPath("$.field_errors[0].field", is("email")))
            .andExpect(jsonPath("$.field_errors[1].field", is("password")));
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
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status", is(401)))
            .andExpect(jsonPath("$.error", is("Unauthorized")))
            .andExpect(jsonPath("$.message", is("Invalid credentials")))
            .andExpect(jsonPath("$.path", is("/api/auth/login")));
    }

    @Test
    void meReturnsAuthenticatedUser() throws Exception {
        when(authService.me(any()))
            .thenReturn(new AuthenticatedUserResponse(1, "Test", "test@example.com"));

        mockMvc.perform(get("/api/auth/me")
                .with(jwt().jwt(token -> token
                    .subject("test@example.com")
                    .tokenValue("jwt-token")
                    .claim("userId", 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("Test")))
            .andExpect(jsonPath("$.email", is("test@example.com")));
    }

    @Test
    void meReturnsUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        when(authService.me(any()))
            .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status", is(401)))
            .andExpect(jsonPath("$.error", is("Unauthorized")))
            .andExpect(jsonPath("$.message", is("Unauthorized")))
            .andExpect(jsonPath("$.path", is("/api/auth/me")));
    }

    @Test
    void logoutClearsAuthenticationCookie() throws Exception {
        when(jwtCookieService.clearAuthenticationCookie()).thenReturn(expiredAuthenticationCookie());

        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string(HttpHeaders.SET_COOKIE, containsString("CHATOP_AUTH=")))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header()
                .string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
            .andExpect(jsonPath("$.message", is("Logout successful")));
    }

    private ResponseCookie authenticationCookie(String token) {
        return ResponseCookie.from("CHATOP_AUTH", token)
            .path("/api")
            .httpOnly(true)
            .build();
    }

    private ResponseCookie expiredAuthenticationCookie() {
        return ResponseCookie.from("CHATOP_AUTH", "")
            .path("/api")
            .httpOnly(true)
            .maxAge(0)
            .build();
    }
}
