package com.chatop.api.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import com.chatop.api.config.properties.ChatopPropertiesTestFactory;

class JwtCookieServiceTest {

    private final JwtCookieService jwtCookieService = new JwtCookieService(
        ChatopPropertiesTestFactory.defaultProperties()
    );

    @Test
    void createsHttpOnlyAuthenticationCookie() {
        ResponseCookie cookie = jwtCookieService.createAuthenticationCookie("jwt-token");

        assertThat(cookie.toString())
            .contains("CHATOP_AUTH=jwt-token")
            .contains("Path=/api")
            .contains("Max-Age=86400")
            .contains("HttpOnly")
            .contains("SameSite=Lax")
            .doesNotContain("Secure");
    }

    @Test
    void createsExpiredCookieToClearAuthenticationCookie() {
        ResponseCookie cookie = jwtCookieService.clearAuthenticationCookie();

        assertThat(cookie.toString())
            .contains("CHATOP_AUTH=")
            .contains("Path=/api")
            .contains("Max-Age=0")
            .contains("HttpOnly");
    }
}
