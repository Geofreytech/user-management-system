package com.im.usermanagement.security.dto;

// This DTO is used to send the JWT token back to the client after successful login
public class AuthResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";

    public AuthResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}