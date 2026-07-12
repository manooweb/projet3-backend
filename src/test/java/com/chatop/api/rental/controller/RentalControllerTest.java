package com.chatop.api.rental.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.config.properties.ChatopPropertiesTestFactory;
import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.dto.RentalSummaryResponse;
import com.chatop.api.rental.dto.RentalsResponse;
import com.chatop.api.rental.dto.UpdateRentalRequest;
import com.chatop.api.rental.service.RentalService;

@WebMvcTest(RentalController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RentalControllerTest.TestConfig.class)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RentalService rentalService;

    static class TestConfig {

        @Bean
        ChatopProperties chatopProperties() {
            return ChatopPropertiesTestFactory.defaultProperties();
        }
    }

    @Test
    void findAllReturnsRentals() throws Exception {
        when(rentalService.findAll())
            .thenReturn(new RentalsResponse(List.of(new RentalSummaryResponse(
                1,
                "House",
                120L,
                950L,
                "http://localhost:9001/api/uploads/rentals/house.jpg",
                "A nice house",
                2,
                LocalDateTime.of(2026, 7, 11, 10, 30),
                LocalDateTime.of(2026, 7, 11, 10, 45)
            ))));

        mockMvc.perform(get("/api/rentals"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rentals[0].id", is(1)))
            .andExpect(jsonPath("$.rentals[0].name", is("House")))
            .andExpect(jsonPath("$.rentals[0].surface", is(120)))
            .andExpect(jsonPath("$.rentals[0].price", is(950)))
            .andExpect(jsonPath("$.rentals[0].picture", is("http://localhost:9001/api/uploads/rentals/house.jpg")))
            .andExpect(jsonPath("$.rentals[0].description", is("A nice house")))
            .andExpect(jsonPath("$.rentals[0].owner_id", is(2)))
            .andExpect(jsonPath("$.rentals[0].created_at", is("2026-07-11T10:30:00")))
            .andExpect(jsonPath("$.rentals[0].updated_at", is("2026-07-11T10:45:00")));

        verify(rentalService).findAll();
    }

    @Test
    void findByIdReturnsRental() throws Exception {
        when(rentalService.findById(1))
            .thenReturn(new RentalSummaryResponse(
                1,
                "House",
                120L,
                950L,
                "http://localhost:9001/api/uploads/rentals/house.jpg",
                "A nice house",
                2,
                LocalDateTime.of(2026, 7, 11, 10, 30),
                LocalDateTime.of(2026, 7, 11, 10, 45)
            ));

        mockMvc.perform(get("/api/rentals/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.name", is("House")))
            .andExpect(jsonPath("$.surface", is(120)))
            .andExpect(jsonPath("$.price", is(950)))
            .andExpect(jsonPath("$.picture", is("http://localhost:9001/api/uploads/rentals/house.jpg")))
            .andExpect(jsonPath("$.description", is("A nice house")))
            .andExpect(jsonPath("$.owner_id", is(2)))
            .andExpect(jsonPath("$.created_at", is("2026-07-11T10:30:00")))
            .andExpect(jsonPath("$.updated_at", is("2026-07-11T10:45:00")));

        verify(rentalService).findById(1);
    }

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

    @Test
    void updateReturnsSuccessMessageWithoutPicture() throws Exception {
        when(rentalService.update(eq(1), any(UpdateRentalRequest.class), nullable(Authentication.class)))
            .thenReturn(new RentalResponse("Rental updated !"));

        mockMvc.perform(multipart("/api/rentals/1")
                .param("name", "House")
                .param("surface", "120")
                .param("price", "950")
                .param("picture", "")
                .param("description", "A nice house")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(jwt().jwt(token -> token
                    .subject("test@example.com")
                    .claim("userId", 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("Rental updated !")));

        verify(rentalService).update(eq(1), any(UpdateRentalRequest.class), nullable(Authentication.class));
    }

    @Test
    void updateReturnsSuccessMessageWithPicture() throws Exception {
        when(rentalService.update(eq(1), any(UpdateRentalRequest.class), nullable(Authentication.class)))
            .thenReturn(new RentalResponse("Rental updated !"));

        mockMvc.perform(multipart("/api/rentals/1")
                .file(picture())
                .param("name", "House")
                .param("surface", "120")
                .param("price", "950")
                .param("description", "A nice house")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .with(jwt().jwt(token -> token
                    .subject("test@example.com")
                    .claim("userId", 1))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message", is("Rental updated !")));

        verify(rentalService).update(eq(1), any(UpdateRentalRequest.class), nullable(Authentication.class));
    }

    @Test
    void updateReturnsBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(multipart("/api/rentals/1")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
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
