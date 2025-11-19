package com.im.usermanagement.Usermanagement.security.dto;

import lombok.Data;

@Data
public class JwtResponseDTO {
    private String token;
    private final String type = "Bearer"; // Conventionally set to "Bearer"

    // Constructor to easily create the response
    public JwtResponseDTO(String token) {
        this.token = token;
    }
}