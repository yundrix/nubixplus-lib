package com.nubixplus.lib.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicateException extends RestResponseException {

    public DuplicateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }

}
