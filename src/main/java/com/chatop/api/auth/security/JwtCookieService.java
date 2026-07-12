package com.chatop.api.auth.security;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.JwtProperties;

@Service
public class JwtCookieService {

    private static final String COOKIE_PATH = "/api";

    private final JwtProperties jwtProperties;

    public JwtCookieService(ChatopProperties chatopProperties) {
        this.jwtProperties = chatopProperties.getJwt();
    }

    public ResponseCookie createAuthenticationCookie(String token) {
        return cookieBuilder(token)
            .maxAge(Duration.ofSeconds(jwtProperties.getExpirationSeconds()))
            .build();
    }

    public ResponseCookie clearAuthenticationCookie() {
        return cookieBuilder("")
            .maxAge(Duration.ZERO)
            .build();
    }

    private ResponseCookie.ResponseCookieBuilder cookieBuilder(String value) {
        return ResponseCookie.from(jwtProperties.getCookieName(), value)
            .httpOnly(true)
            .secure(jwtProperties.isCookieSecure())
            .sameSite(jwtProperties.getCookieSameSite())
            .path(COOKIE_PATH);
    }
}
