package com.im.usermanagement.exception; // 👈 MUST BE PRESENT

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}