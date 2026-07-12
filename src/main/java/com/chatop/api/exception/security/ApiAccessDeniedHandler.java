package com.chatop.api.exception.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ErrorMessagesProperties;
import com.chatop.api.exception.ApiErrorResponseWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ApiErrorResponseWriter errorResponseWriter;
    private final ErrorMessagesProperties errors;

    public ApiAccessDeniedHandler(
        ApiErrorResponseWriter errorResponseWriter,
        ChatopProperties chatopProperties
    ) {
        this.errorResponseWriter = errorResponseWriter;
        this.errors = chatopProperties.getErrors();
    }

    @Override
    public void handle(
        HttpServletRequest request,
        HttpServletResponse response,
        AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        errorResponseWriter.write(response, HttpStatus.FORBIDDEN, errors.getForbidden(), request.getRequestURI());
    }
}
