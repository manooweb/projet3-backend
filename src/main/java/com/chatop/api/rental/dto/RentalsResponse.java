package com.chatop.api.rental.dto;

import java.util.List;

public record RentalsResponse(List<RentalSummaryResponse> rentals) {
}
