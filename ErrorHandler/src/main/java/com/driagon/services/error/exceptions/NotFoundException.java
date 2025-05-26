package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class NotFoundException extends BaseException {

    @Serial
    private static final long serialVersionUID = 5577442278413126919L;

    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}