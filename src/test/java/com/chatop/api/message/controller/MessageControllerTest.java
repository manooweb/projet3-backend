package com.chatop.api.message.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.message.dto.CreateMessageRequest;
import com.chatop.api.message.dto.MessageResponse;
import com.chatop.api.message.service.MessageService;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessageService messageService;

    @Test
    void createReturnsSuccessMessage() throws Exception {
        when(messageService.create(any(CreateMessageRequest.class), nullable(Authentication.class)))
            .thenReturn(new MessageResponse("Message send with success"));

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "rental_id": 1,
                      "user_id": 2,
                      "message": "Hello, I am interested in this rental"
                    }
                    """)
                .with(jwt().jwt(token -> token
                    .subject("test@example.com")
                    .claim("userId", 2))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.message", is("Message send with success")));

        verify(messageService).create(any(CreateMessageRequest.class), nullable(Authentication.class));
    }

    @Test
    void createReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "message": ""
                    }
                    """)
                .with(jwt().jwt(token -> token.claim("userId", 2))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status", is(400)))
            .andExpect(jsonPath("$.error", is("Bad Request")))
            .andExpect(jsonPath("$.message", is("Validation failed")))
            .andExpect(jsonPath("$.path", is("/api/messages")))
            .andExpect(jsonPath("$.field_errors[0].field", is("message")))
            .andExpect(jsonPath("$.field_errors[1].field", is("rentalId")))
            .andExpect(jsonPath("$.field_errors[2].field", is("userId")));
    }
}
