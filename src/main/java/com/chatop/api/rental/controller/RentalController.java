package com.chatop.api.rental.controller;

import java.beans.PropertyEditorSupport;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.dto.RentalSummaryResponse;
import com.chatop.api.rental.dto.RentalsResponse;
import com.chatop.api.rental.dto.UpdateRentalRequest;
import com.chatop.api.rental.service.RentalService;

import jakarta.validation.Valid;

/**
 * Exposes rental listing, detail, creation and update endpoints.
 */
@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    private final RentalService rentalService;
    private final ChatopProperties chatopProperties;

    public RentalController(RentalService rentalService, ChatopProperties chatopProperties) {
        this.rentalService = rentalService;
        this.chatopProperties = chatopProperties;
    }

    @InitBinder
    void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MultipartFile.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                    return;
                }

                throw new IllegalArgumentException(chatopProperties.getErrors().getPictureFileRequired());
            }
        });
    }

    /**
     * Lists all rentals available through the API.
     *
     * @return the rentals collection
     */
    @GetMapping
    public RentalsResponse findAll() {
        return rentalService.findAll();
    }

    /**
     * Returns the details of a single rental.
     *
     * @param id the rental identifier
     * @return the rental details
     */
    @GetMapping("/{id}")
    public RentalSummaryResponse findById(@PathVariable Integer id) {
        return rentalService.findById(id);
    }

    /**
     * Creates a rental owned by the authenticated user.
     *
     * @param request the multipart rental payload including the required picture
     * @param authentication the authenticated principal resolved by Spring Security
     * @return a success response once the rental has been created
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RentalResponse create(
        @Valid @ModelAttribute CreateRentalRequest request,
        Authentication authentication
    ) {
        return rentalService.create(request, authentication);
    }

    /**
     * Updates an existing rental owned by the authenticated user.
     *
     * @param id the rental identifier
     * @param request the multipart rental payload, with an optional replacement picture
     * @param authentication the authenticated principal resolved by Spring Security
     * @return a success response once the rental has been updated
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RentalResponse update(
        @PathVariable Integer id,
        @Valid @ModelAttribute UpdateRentalRequest request,
        Authentication authentication
    ) {
        return rentalService.update(id, request, authentication);
    }
}
