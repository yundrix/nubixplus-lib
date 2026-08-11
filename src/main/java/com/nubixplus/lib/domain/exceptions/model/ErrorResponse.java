package com.nubixplus.lib.domain.exceptions.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatusCode;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(HttpStatusCode code, String message, List<ValidationError> errors) {

    public static ErrorResponse of(HttpStatusCode code, String message) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(String message, HttpStatusCode code) {
        return new ErrorResponse(code, message, null);
    }

    public static ErrorResponse of(HttpStatusCode code, String message, List<ValidationError> errors) {
        return new ErrorResponse(code, message, errors);
    }

    public static ErrorResponse of(String message, HttpStatusCode code, List<ValidationError> errors) {
        return new ErrorResponse(code, message, errors);
    }
}
