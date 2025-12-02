package com.im.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception to indicate that a requested resource (like a User)
 * does not exist. Spring will automatically translate this to an HTTP 404 response.
 */
@ResponseStatus(HttpStatus.NOT_FOUND) // Ensures a 404 status code is returned
public class ResourceNotFoundException extends RuntimeException {

    // Standard constructor that accepts a message
    public ResourceNotFoundException(String message) {
        super(message);
    }
}