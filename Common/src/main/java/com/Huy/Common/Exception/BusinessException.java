package com.Huy.Common.Exception;

public class BusinessException extends RuntimeException {
    private String errorCode;
    
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
