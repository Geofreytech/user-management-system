package com.im.usermanagement.controller;

import com.im.usermanagement.model.User;
import com.im.usermanagement.service.UserService;
import com.im.usermanagement.exception.ResourceNotFoundException;
import com.im.usermanagement.exception.UserAlreadyExistsException;

import org.springframework.http.HttpStatus; // Used to set custom HTTP status codes
import org.springframework.http.ResponseEntity; // Wrapper for the response
import org.springframework.web.bind.annotation.*; // Contains all HTTP mapping annotations

import java.util.List;

@RestController // 1. Combines @Controller and @ResponseBody (returns JSON/XML)
@RequestMapping("/api/v1/users") // 2. Base URL for all methods in this controller
public class UserController {

    private final UserService userService; // Dependency on the Service Layer

    // Constructor Injection (Spring automatically wires the UserService bean)
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- 1. GET: Retrieve All Users ---
    @GetMapping // Maps to GET /api/v1/users
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // Returns the list of users with an HTTP 200 OK status
        return ResponseEntity.ok(users);
    }

    // --- 2. GET: Retrieve User by ID ---
    @GetMapping("/{id}") // Maps to GET /api/v1/users/{id} (e.g., /api/v1/users/1)
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        // The service method throws ResourceNotFoundException if user isn't found
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user); // HTTP 200 OK
    }

    // --- 3. POST: Create a New User ---
    @PostMapping // Maps to POST /api/v1/users
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        // Returns the created user object with an HTTP 201 Created status
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    // --- 4. PUT: Update an Existing User ---
    @PutMapping("/{id}") // Maps to PUT /api/v1/users/{id}
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        User updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser); // HTTP 200 OK
    }

    // --- 5. DELETE: Soft Delete a User ---
    @DeleteMapping("/{id}") // Maps to DELETE /api/v1/users/{id}
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.softDeleteUser(id);
        // Returns an empty response with an HTTP 204 No Content status (Success, no body needed)
        return ResponseEntity.noContent().build();
    }
}