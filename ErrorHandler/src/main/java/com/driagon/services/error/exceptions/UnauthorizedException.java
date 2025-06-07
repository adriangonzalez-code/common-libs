package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class UnauthorizedException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1370808989352513977L;

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}