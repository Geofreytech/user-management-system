package com.im.usermanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;


import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice // 1. Makes this class handle exceptions across all Controllers
public class GlobalExceptionHandler {

    // --- 404 NOT FOUND Handler ---
    @ExceptionHandler(ResourceNotFoundException.class) // 2. Specifies which exception to catch
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        // 3. Custom error response structure (standard practice)
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // 4. Return 404 Not Found Status
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // --- 409 CONFLICT/400 BAD REQUEST Handler ---
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        // Return 409 Conflict Status (often used for unique constraint violations)
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
}