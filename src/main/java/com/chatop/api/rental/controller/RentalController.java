package com.chatop.api.rental.controller;

import static com.chatop.api.config.OpenApiConfig.COOKIE_AUTH_SECURITY_SCHEME;

import java.beans.PropertyEditorSupport;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.multipart.MultipartFile;

import com.chatop.api.config.properties.ChatopProperties;
import com.chatop.api.exception.dto.ApiErrorResponse;
import com.chatop.api.rental.dto.CreateRentalRequest;
import com.chatop.api.rental.dto.RentalResponse;
import com.chatop.api.rental.dto.RentalSummaryResponse;
import com.chatop.api.rental.dto.RentalsResponse;
import com.chatop.api.rental.dto.UpdateRentalRequest;
import com.chatop.api.rental.service.RentalService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes rental listing, detail, creation and update endpoints.
 */
@Tag(name = "Rentals", description = "Rental listing and management endpoints")
@SecurityRequirement(name = COOKIE_AUTH_SECURITY_SCHEME)
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
    @Operation(summary = "List rentals", description = "Returns all rentals.")
    @ApiResponse(
        responseCode = "200",
        description = "Rentals found",
        content = @Content(schema = @Schema(implementation = RentalsResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Unexpected server error",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
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
    @Operation(summary = "Get rental", description = "Returns the details of a rental.")
    @ApiResponse(
        responseCode = "200",
        description = "Rental found",
        content = @Content(schema = @Schema(implementation = RentalSummaryResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Rental not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Unexpected server error",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @GetMapping("/{id}")
    public RentalSummaryResponse findById(@Parameter(description = "Rental ID") @PathVariable Integer id) {
        return rentalService.findById(id);
    }

    /**
     * Creates a rental owned by the authenticated user.
     *
     * @param request the multipart rental payload including the required picture
     * @param authentication the authenticated principal resolved by Spring Security
     * @return a success response once the rental has been created
     */
    @Operation(summary = "Create rental", description = "Creates a rental owned by the authenticated user.")
    @ApiResponse(
        responseCode = "201",
        description = "Rental created",
        content = @Content(schema = @Schema(implementation = RentalResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "CSRF token is missing or invalid",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Unexpected server error",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse create(
        @Valid @ModelAttribute CreateRentalRequest request,
        @Parameter(hidden = true) Authentication authentication
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
    @Operation(summary = "Update rental", description = "Updates an existing rental owned by the authenticated user.")
    @ApiResponse(
        responseCode = "200",
        description = "Rental updated",
        content = @Content(schema = @Schema(implementation = RentalResponse.class))
    )
    @ApiResponse(
        responseCode = "400",
        description = "Validation error",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "401",
        description = "Authentication is required",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "403",
        description = "Rental ownership or CSRF token is invalid",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "404",
        description = "Rental not found",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @ApiResponse(
        responseCode = "500",
        description = "Unexpected server error",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
    )
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RentalResponse update(
        @Parameter(description = "Rental ID") @PathVariable Integer id,
        @Valid @ModelAttribute UpdateRentalRequest request,
        @Parameter(hidden = true) Authentication authentication
    ) {
        return rentalService.update(id, request, authentication);
    }
}
