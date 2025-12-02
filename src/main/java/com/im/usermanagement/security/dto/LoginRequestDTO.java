package com.im.usermanagement.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// This DTO is used to receive user credentials during login
public class LoginRequestDTO {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    // Default constructor
    public LoginRequestDTO() {}

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}