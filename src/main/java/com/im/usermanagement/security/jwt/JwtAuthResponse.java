package com.im.usermanagement.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for the successful authentication response.
 * Its purpose is to carry the generated JWT access token back to the client.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {

    // 1. This is the actual token the client needs for subsequent requests.
    private String accessToken;

    // 2. This field tells the client the type of token (standard is "Bearer").
    @Builder.Default
    private String tokenType = "Bearer";

    // Note: This DTO does not need any fields or methods related to email.
}