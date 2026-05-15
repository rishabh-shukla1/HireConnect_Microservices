package com.hireconnect.application.exception;

public class UnauthorizedApplicationAccessException extends RuntimeException {

    public UnauthorizedApplicationAccessException(String message) {
        super(message);
    }
}