package com.chatop.api.exception.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ApiErrorResponse(
    int status,
    String error,
    String message,
    String path,
    @JsonProperty("field_errors")
    List<FieldValidationError> fieldErrors
) {

    public record FieldValidationError(
        String field,
        String message
    ) {
    }
}
