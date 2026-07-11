package com.chatop.api.auth.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.auth.dto.AuthTokenResponse;
import com.chatop.api.auth.dto.RegisterRequest;
import com.chatop.api.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthTokenResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
