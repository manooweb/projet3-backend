package com.chatop.api.message.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateMessageRequest(
    @JsonProperty("rental_id")
    @NotNull
    Integer rentalId,

    @JsonProperty("user_id")
    @NotNull
    Integer userId,

    @NotBlank
    @Size(max = 2000)
    String message
) {
}
