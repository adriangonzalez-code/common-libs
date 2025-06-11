package com.driagon.services.error.handler;

import com.driagon.services.error.exceptions.BaseException;
import com.driagon.services.error.models.ErrorDetail;
import com.driagon.services.error.models.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(ex.getStatus().name());
        error.setCode(ex.getCode());
        error.setMessage(ex.getMessage());
        error.setPath(request.getDescription(false));

        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidationErrors(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.name());
        error.setCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage("Error de validación");
        error.setPath(request.getDescription(false));

        if (ex instanceof MethodArgumentNotValidException manvEx) {
            manvEx.getBindingResult().getFieldErrors().forEach(err ->
                    error.getDetails().add(new ErrorDetail(err.getField(), err.getDefaultMessage(), "INVALID_FIELD"))
            );
        } else if (ex instanceof ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach(violation ->
                    error.getDetails().add(new ErrorDetail(violation.getPropertyPath().toString(), violation.getMessage(), "INVALID_FIELD"))
            );
        }

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ServletException.class)
    public ResponseEntity<ErrorResponse> handleSpringExceptions(ServletException ex, WebRequest request) {
        if (ex instanceof org.springframework.web.ErrorResponse exception) {
            ErrorResponse error = new ErrorResponse();
            error.setStatus(HttpStatus.valueOf(exception.getStatusCode().value()).name());
            error.setCode(exception.getStatusCode().value());
            error.setMessage(exception.getBody().getDetail());
            error.setPath(request.getDescription(false));
            return new ResponseEntity<>(error, HttpStatus.valueOf(exception.getStatusCode().value()));
        }

        ErrorResponse fallback = new ErrorResponse();
        fallback.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        fallback.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        fallback.setMessage("Error de servidor inesperado");
        fallback.setPath(request.getDescription(false));
        return new ResponseEntity<>(fallback, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @Order
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.name());
        error.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage("Se ha producido un error en el servidor: " + ex.getMessage());
        error.setPath(request.getDescription(false));

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}