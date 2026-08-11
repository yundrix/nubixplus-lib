package com.nubixplus.lib.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED)
public class MethodNotAllowedException extends RestResponseException {

    public MethodNotAllowedException(String message) {
        super(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

}
