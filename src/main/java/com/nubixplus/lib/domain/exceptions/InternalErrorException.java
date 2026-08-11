package com.nubixplus.lib.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalErrorException extends RestResponseException {

    public InternalErrorException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

}
