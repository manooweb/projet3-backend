package com.chatop.api.auth.security;

import java.util.Arrays;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import com.chatop.api.config.properties.ChatopProperties;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtCookieBearerTokenResolver implements BearerTokenResolver {

    private final String cookieName;

    public JwtCookieBearerTokenResolver(ChatopProperties chatopProperties) {
        this.cookieName = chatopProperties.getJwt().getCookieName();
    }

    @Override
    public String resolve(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
            .filter(cookie -> cookieName.equals(cookie.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }
}
