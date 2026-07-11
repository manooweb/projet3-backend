package com.chatop.api.rental.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RentalSummaryResponse(
    Integer id,
    String name,
    Long surface,
    Long price,
    String picture,
    String description,
    @JsonProperty("owner_id")
    Integer ownerId,
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    @JsonProperty("updated_at")
    LocalDateTime updatedAt
) {
}
