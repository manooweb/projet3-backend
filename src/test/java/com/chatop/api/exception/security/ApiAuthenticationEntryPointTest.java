package com.chatop.api.exception.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.chatop.api.config.properties.ChatopPropertiesTestFactory;
import com.chatop.api.exception.ApiErrorResponseWriter;

import tools.jackson.databind.json.JsonMapper;

class ApiAuthenticationEntryPointTest {

    @Test
    void commenceWritesUnauthorizedErrorResponse() throws Exception {
        ApiAuthenticationEntryPoint entryPoint = new ApiAuthenticationEntryPoint(
            new ApiErrorResponseWriter(JsonMapper.builder().build()),
            ChatopPropertiesTestFactory.defaultProperties()
        );
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rentals");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Unauthorized"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo(StandardCharsets.UTF_8.name());
        assertThat(response.getContentAsString()).contains(
            "\"status\":401",
            "\"error\":\"Unauthorized\"",
            "\"message\":\"Unauthorized\"",
            "\"path\":\"/api/rentals\""
        );
    }
}
