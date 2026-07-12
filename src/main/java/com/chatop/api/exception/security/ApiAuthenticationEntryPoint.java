package com.chatop.api.exception.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ErrorMessagesProperties;
import com.chatop.api.exception.ApiErrorResponseWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ApiErrorResponseWriter errorResponseWriter;
    private final ErrorMessagesProperties errors;

    public ApiAuthenticationEntryPoint(
        ApiErrorResponseWriter errorResponseWriter,
        ChatopProperties chatopProperties
    ) {
        this.errorResponseWriter = errorResponseWriter;
        this.errors = chatopProperties.getErrors();
    }

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {
        errorResponseWriter.write(response, HttpStatus.UNAUTHORIZED, errors.getUnauthorized(), request.getRequestURI());
    }
}
