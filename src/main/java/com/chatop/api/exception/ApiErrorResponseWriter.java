package com.chatop.api.exception;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.chatop.api.exception.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@Component
public class ApiErrorResponseWriter {

    private final JsonMapper jsonMapper;

    public ApiErrorResponseWriter(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public void write(
        HttpServletResponse response,
        HttpStatus status,
        String message,
        String path
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        jsonMapper.writeValue(response.getWriter(), new ApiErrorResponse(
            status.value(),
            status.getReasonPhrase(),
            message,
            path,
            List.of()
        ));
    }
}
