package com.chatop.api.rental.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.service.RentalService;

@WebMvcTest(RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentalService rentalService;

    @Test
    void createReturnsSuccessMessage() throws Exception {
        when(rentalService.create(any(CreateRentalRequest.class), nullable(Authentication.class)))
            .thenReturn(new RentalResponse("Rental created !"));

        mockMvc.perform(multipart("/api/rentals")
                .file(picture())
                .param("name", "House")
                .param("surface", "120")
                .param("price", "950")
                .param("description", "A nice house")
                .with(jwt().jwt(token -> token
                    .subject("test@example.com")
                    .claim("userId", 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("Rental created !")));

        verify(rentalService).create(any(CreateRentalRequest.class), nullable(Authentication.class));
    }

    @Test
    void createReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(multipart("/api/rentals")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(jwt().jwt(token -> token.claim("userId", 1))))
            .andExpect(status().isBadRequest());
    }

    private MockMultipartFile picture() {
        return new MockMultipartFile(
            "picture",
            "house.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image-content".getBytes()
        );
    }
}
