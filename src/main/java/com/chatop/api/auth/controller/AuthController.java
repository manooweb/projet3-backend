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

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return authenticatedResponse(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return authenticatedResponse(authService.login(request));
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me(Authentication authentication) {
        return authService.me(authentication);
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken csrfToken) {
        csrfToken.getToken();

        return ResponseEntity.noContent().build();
    }

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
