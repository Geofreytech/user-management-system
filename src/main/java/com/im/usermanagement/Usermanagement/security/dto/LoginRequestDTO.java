package com.im.usermanagement.Usermanagement.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for handling user login requests.
 * Lombok's @Data annotation generates the getEmail() and getPassword() methods
 * that the AuthController requires.
 */
@Data // This generates all getters (like getEmail), setters, toString, equals, and hashCode.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is required")
    private String email; // The method getEmail() is generated for this field.

    @NotBlank(message = "Password is required")
    private String password;
}