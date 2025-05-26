package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ForbiddenException extends BaseException {

    @Serial
    private static final long serialVersionUID = 4022635404783882674L;

    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}