package com.chatop.api.rental.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RentalSummaryResponse(
    Integer id,
    String name,
    BigDecimal surface,
    BigDecimal price,
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
