package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public abstract class BaseException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 3207165888391250459L;

    private final HttpStatus status;
    private final int code;
    private final String message;

    public BaseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.code = status.value();
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}