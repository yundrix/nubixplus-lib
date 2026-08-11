package com.nubixplus.lib.domain.exceptions;

import com.nubixplus.lib.domain.exceptions.model.ErrorResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;

/**
 * Raiz de las excepciones de negocio. Cada especializacion fija su estado HTTP, de
 * modo que el {@code RestExceptionHandler} traduce cualquiera de ellas sin conocer
 * el caso concreto.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public abstract class RestResponseException extends RestClientResponseException {

    private final HttpStatus status;

    RestResponseException(HttpStatus status, String message) {
        super(message, status, message, null, new byte[0], StandardCharsets.UTF_8);
        this.status = status;
    }

    public ErrorResponse asError() {
        return ErrorResponse.of(super.getMessage(), this.status);
    }

}
