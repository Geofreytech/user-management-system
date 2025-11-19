package com.im.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for business rule conflicts, specifically when a user attempts
 * to register with an email already in use. Spring will automatically translate
 * this to an HTTP 400 response.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // Ensures a 400 status code is returned
public class UserAlreadyExistsException extends RuntimeException {

    // Standard constructor that accepts a message
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}