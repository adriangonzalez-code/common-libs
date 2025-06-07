package com.driagon.services.error.exceptions;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ServiceUnavailableException extends BaseException {

    @Serial
    private static final long serialVersionUID = 6735817592517862544L;

    public ServiceUnavailableException(String message) {
        super(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
}