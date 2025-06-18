package com.Huy.Common.Exception;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;
    
    public ValidationErrorResponse(String errorCode, String message, Map<String, String> errors) {
        super(errorCode, message);
        this.errors = errors;
    }
}