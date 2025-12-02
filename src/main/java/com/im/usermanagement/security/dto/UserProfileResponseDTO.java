package com.im.usermanagement.security.dto;

import lombok.Builder;
import lombok.Data;

// This DTO defines the exact data structure returned to the authenticated client
@Data
@Builder
public class UserProfileResponseDTO {
    private Long id;
    private String username; // Or email, depending on what you use as the identifier
    private String email;
    private String firstName;
    private String lastName;
    private String role; // The role (e.g., ROLE_USER) derived from the User entity
}