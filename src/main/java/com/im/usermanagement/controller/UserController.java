package com.im.usermanagement.controller;

import com.im.usermanagement.security.dto.UserProfileResponseDTO;
import com.im.usermanagement.security.dto.RegisterRequestDTO; // <-- ADDED: Import the DTO used for creation
import com.im.usermanagement.model.User;
import com.im.usermanagement.service.AuthService; // <-- ADDED: Import AuthService
import com.im.usermanagement.service.UserService;
import com.im.usermanagement.exception.ResourceNotFoundException;

import jakarta.validation.Valid; // Required for @Valid annotation on DTO
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing all users (CRUD) and handling the authenticated user's profile (/me).
 * CRUD methods are restricted to ROLE_ADMIN using method-level security.
 * The base path for this controller is /api/v1/users.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService; // <-- ADDED: Field for AuthService

    // Constructor Injection
    public UserController(UserService userService, AuthService authService) { // <-- MODIFIED: Inject AuthService
        this.userService = userService;
        this.authService = authService; // <-- Initialize AuthService
    }

    // --- Secured Endpoint: Get Current User's Profile ---
    /**
     * Endpoint to retrieve the profile details of the currently logged-in user.
     * Accessible by any authenticated user.
     * Maps to GET /api/v1/users/me
     * @return UserProfileResponseDTO containing non-sensitive profile data.
     */
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDTO> getCurrentUser() {
        // This leverages the service method we defined, which securely pulls the
        // authenticated user's ID from the SecurityContextHolder.
        UserProfileResponseDTO profile = userService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
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
        // Assuming your service handles ResourceNotFoundException
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user); // HTTP 200 OK
    }

    // --- 3. POST: Create a New User (Admin Only) ---
    /**
     * Endpoint for creating a new user, allowing role assignment (ADMIN or USER)
     * based on the input DTO. Requires the caller to have the ADMIN role.
     * @param request The DTO containing the user's details and the desired 'role' field.
     * @return The created User entity.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping // Maps to POST /api/v1/users
    public ResponseEntity<User> createUser(@Valid @RequestBody RegisterRequestDTO request) { // <-- MODIFIED to use DTO
        // The isAdminContext=true flag tells the AuthService to check the DTO for a requested role (like "ADMIN")
        User createdUser = authService.register(request, true); // <-- MODIFIED to use AuthService
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // HTTP 201 Created
    }

    // --- 4. PUT: Update an Existing User (Admin Only) ---
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}") // Maps to PUT /api/v1/users/{id}
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        // You might consider changing this to a DTO as well for safer updates
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