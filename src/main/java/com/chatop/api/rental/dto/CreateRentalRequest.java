package com.chatop.api.rental.dto;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRentalRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    @Positive
    private Long surface;

    @NotNull
    @Positive
    private Long price;

    @Schema(type = "string", format = "binary")
    @NotNull
    private MultipartFile picture;

    @NotBlank
    @Size(max = 2000)
    private String description;
}
