package com.chatop.api.rental.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.service.RentalService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RentalResponse create(
        @Valid @ModelAttribute CreateRentalRequest request,
        Authentication authentication
    ) {
        return rentalService.create(request, authentication);
    }
}
