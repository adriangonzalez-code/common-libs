package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ProcessException extends BaseException {

    @Serial
    private static final long serialVersionUID = -1134772217304503426L;

    public ProcessException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}