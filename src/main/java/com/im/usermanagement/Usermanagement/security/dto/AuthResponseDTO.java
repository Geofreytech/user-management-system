package com.im.usermanagement.Usermanagement.security.dto;

import lombok.Data;
import lombok.NoArgsConstructor; // Ensure this is present
import lombok.AllArgsConstructor; // Add this for the full constructor

@Data
@NoArgsConstructor // Required if your DTO is used in Spring/Jackson deserialization
@AllArgsConstructor // ⭐ NEW: Generates a constructor with all fields (accessToken and tokenType)
public class AuthResponseDTO {

    private String accessToken;
    private String tokenType = "Bearer";

    // ⭐ FIX: Add this explicit constructor back to resolve the error in AuthController
    // The @AllArgsConstructor might cover this, but explicitly defining it is safer.
    public AuthResponseDTO(String accessToken) {
        this.accessToken = accessToken;
        // tokenType defaults to "Bearer" but can be set explicitly
        this.tokenType = "Bearer";
    }
}