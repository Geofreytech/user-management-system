package com.im.usermanagement.controller;

import com.im.usermanagement.Usermanagement.security.dto.UserResponseDTO; // For the secured /me endpoint response
import com.im.usermanagement.model.User;
import com.im.usermanagement.service.UserService;
import com.im.usermanagement.exception.ResourceNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // REQUIRED for method-level security
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for managing all users (CRUD) and handling the authenticated user's profile (/me).
 * CRUD methods are restricted to ROLE_ADMIN using method-level security.
 * The base path for this controller is /api/v1/users.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    // Constructor Injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- Secured Endpoint: Get Current User ---
    /**
     * Endpoint to retrieve the details of the currently logged-in user.
     * Accessible by any authenticated user.
     * Maps to GET /api/v1/users/me
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the principal is a UserDetails object
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {

            // In our system, the principal is the actual User entity which implements UserDetails
            // If we used the principal from the token (JwtTokenProvider) it would be a Spring UserDetails object.
            // Since the JwtTokenProvider currently recreates a simple Spring UserDetails object
            // instead of fetching the User entity, we'll rely on the username (email) and fetch the full User object.

            String email = authentication.getName();

            try {
                // Fetch the full user details from the database
                User user = userService.getUserByEmail(email); // Requires a new method in UserService

                // Map the User entity to the response DTO
                UserResponseDTO response = UserResponseDTO.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        // Map authorities (roles) to a list of strings
                        .roles(user.getAuthorities().stream()
                                .map(Object::toString)
                                .collect(Collectors.toList()))
                        .build();

                return ResponseEntity.ok(response);
            } catch (ResourceNotFoundException e) {
                // This should not happen if the user is authenticated, but provides a fallback
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    // --------------------------------------------------------------------------
    // --- ADMIN-ONLY CRUD Operations ---
    // --------------------------------------------------------------------------

    // --- 1. GET: Retrieve All Users (Admin Only) ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping // Maps to GET /api/v1/users
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users); // HTTP 200 OK
    }

    // --- 2. GET: Retrieve User by ID (Admin Only) ---
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}") // Maps to GET /api/v1/users/{id}
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user); // HTTP 200 OK
    }

    // --- 3. POST: Create a New User (Admin Only) ---
    // NOTE: This should ideally use a DTO and a dedicated registration service method
    // but for simplicity, we'll keep the User entity input.
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping // Maps to POST /api/v1/users
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // HTTP 201 Created
    }

    // --- 4. PUT: Update an Existing User (Admin Only) ---
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}") // Maps to PUT /api/v1/users/{id}
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser); // HTTP 200 OK
    }

    // --- 5. DELETE: Soft Delete a User (Admin Only) ---
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}") // Maps to DELETE /api/v1/users/{id}
    public ResponseEntity<Void> softDeleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}