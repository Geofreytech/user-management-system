package com.im.usermanagement.exception;

// This will map to an HTTP 404 Not Found error
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}