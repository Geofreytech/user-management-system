package com.im.usermanagement.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing the details of the currently authenticated user
 * (used for the /me endpoint response).
 * It ensures we only return necessary user fields (like ID, name, email, roles)
 * and never the hashed password, which enhances security.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
}