package com.chatop.api.exception.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import com.chatop.api.config.properties.ChatopPropertiesTestFactory;
import com.chatop.api.exception.ApiErrorResponseWriter;

import tools.jackson.databind.json.JsonMapper;

class ApiAccessDeniedHandlerTest {

    @Test
    void handleWritesForbiddenErrorResponse() throws Exception {
        ApiAccessDeniedHandler handler = new ApiAccessDeniedHandler(
            new ApiErrorResponseWriter(JsonMapper.builder().build()),
            ChatopPropertiesTestFactory.defaultProperties()
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rentals/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Forbidden"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentAsString()).contains(
            "\"status\":403",
            "\"error\":\"Forbidden\"",
            "\"message\":\"Forbidden\"",
            "\"path\":\"/api/rentals/1\""
        );
    }
}
