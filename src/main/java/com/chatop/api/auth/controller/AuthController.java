package com.chatop.api.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.auth.dto.AuthResponse;
import com.chatop.api.auth.dto.AuthenticatedUserResponse;
import com.chatop.api.auth.dto.LoginRequest;
import com.chatop.api.auth.dto.RegisterRequest;
import com.chatop.api.auth.security.JwtCookieService;
import com.chatop.api.auth.service.AuthService;
import com.chatop.api.config.properties.ResponseMessagesProperties;

/**
 * Exposes authentication endpoints used by the ChaTop client.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtCookieService jwtCookieService;
    private final ResponseMessagesProperties responses;

    public AuthController(
        AuthService authService,
        JwtCookieService jwtCookieService,
        ResponseMessagesProperties responses
    ) {
        this.authService = authService;
        this.jwtCookieService = jwtCookieService;
        this.responses = responses;
    }

    /**
     * Registers a new user account and starts an authenticated session.
     *
     * @param request the registration payload containing the user's name, email and password
     * @return a success response and an authentication cookie
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authenticatedResponse(authService.register(request));
    }

    /**
     * Authenticates an existing user and starts an authenticated session.
     *
     * @param request the login payload containing the user's email and password
     * @return a success response and an authentication cookie
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authenticatedResponse(authService.login(request));
    }

    /**
     * Returns the currently authenticated user's public profile.
     *
     * @param authentication the authenticated principal resolved by Spring Security
     * @return the authenticated user's public information
     */
    @GetMapping("/me")
    public AuthenticatedUserResponse me(Authentication authentication) {
        return authService.me(authentication);
    }

    /**
     * Initializes the CSRF token for browser clients.
     *
     * @param csrfToken the CSRF token resolved by Spring Security
     * @return an empty response once the CSRF token has been resolved
     */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();

        return ResponseEntity.noContent().build();
    }

    /**
     * Clears the authentication cookie for the current client.
     *
     * @return a success response and an expired authentication cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout() {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookieService.clearAuthenticationCookie().toString())
            .body(new AuthResponse(responses.getLogoutSuccessful()));
    }

    private ResponseEntity<AuthResponse> authenticatedResponse(String token) {
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookieService.createAuthenticationCookie(token).toString())
            .body(new AuthResponse(responses.getAuthenticationSuccessful()));
    }
}
