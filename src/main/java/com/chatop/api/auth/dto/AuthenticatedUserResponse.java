package com.chatop.api.auth.dto;

public record AuthenticatedUserResponse(
    Integer id,
    String name,
    String email
) {
}
