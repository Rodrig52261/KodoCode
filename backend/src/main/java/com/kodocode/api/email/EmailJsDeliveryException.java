package com.kodocode.api.email;

public class EmailJsDeliveryException extends RuntimeException {
    public EmailJsDeliveryException(String message) {
        super(message);
    }

    public EmailJsDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }
}
