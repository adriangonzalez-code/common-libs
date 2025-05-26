package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class BusinessException extends BaseException {

    @Serial
    private static final long serialVersionUID = -2080732459766532030L;

    public BusinessException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}