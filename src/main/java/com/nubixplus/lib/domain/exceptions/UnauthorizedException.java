package com.nubixplus.lib.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RestResponseException {

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }

    public UnauthorizedException() {
        this("No autenticado.");
    }
}
