package com.Huy.Common.Exception;

public class BadPaymentRequestException extends RuntimeException {
    public BadPaymentRequestException(String message) {
        super(message);
    }
}
