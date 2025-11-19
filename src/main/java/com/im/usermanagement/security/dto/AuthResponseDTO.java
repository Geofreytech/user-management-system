package com.im.usermanagement.security.dto; // FIXED

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sending the JWT token back to the client after a successful login.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String accessToken;
    private final String tokenType = "Bearer"; // Always fixed for consistency
}