package com.nubixplus.lib.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RestResponseException {

    public BadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
