package com.driagon.services.error.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serial;
import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1715356951970522168L;

    private String field;
    private String message;
    private String code;

    public ErrorDetail() {
    }

    public ErrorDetail(String field, String message, String code) {
        this.field = field;
        this.message = message;
        this.code = code;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}