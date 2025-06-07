package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class InvalidArgumentsException extends BaseException {

    @Serial
    private static final long serialVersionUID = 9037271345982854864L;

    public InvalidArgumentsException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}