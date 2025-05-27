package com.driagon.services.error.handler;

import com.driagon.services.error.exceptions.BaseException;
import com.driagon.services.error.models.ErrorDetail;
import com.driagon.services.error.models.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(ex.getStatus().name());
        error.setCode(ex.getCode());
        error.setMessage(ex.getMessage());
        error.setPath(request.getDescription(false));

        log.error("Error: {}", error);
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.name());
        error.setCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage("Error de validación");
        error.setPath(request.getDescription(false));

        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                error.getDetails().add(new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage(), "INVALID_FIELD"))
        );

        log.error("Validation error: {}", error);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage(ex.getMessage());
        error.setPath(request.getDescription(false));

        log.error("Uncaught exception: {}", error);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}