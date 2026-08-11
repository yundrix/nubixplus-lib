package com.nubixplus.lib.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class ForbiddenException extends RestResponseException {

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public ForbiddenException() {
        this("No tiene permisos para realizar esta accion.");
    }
}
