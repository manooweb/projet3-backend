package com.chatop.api.auth.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import com.chatop.api.config.properties.ChatopPropertiesTestFactory;

import jakarta.servlet.http.Cookie;

class JwtCookieBearerTokenResolverTest {

    private final JwtCookieBearerTokenResolver resolver = new JwtCookieBearerTokenResolver(
        ChatopPropertiesTestFactory.defaultProperties()
    );

    @Test
    void resolvesJwtFromAuthenticationCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(
            new Cookie("other", "ignored"),
            new Cookie("CHATOP_AUTH", "jwt-token")
        );

        assertThat(resolver.resolve(request)).isEqualTo("jwt-token");
    }

    @Test
    void returnsNullWhenAuthenticationCookieIsMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "ignored"));

        assertThat(resolver.resolve(request)).isNull();
    }
}
