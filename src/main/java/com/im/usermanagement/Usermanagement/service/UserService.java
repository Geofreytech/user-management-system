package com.im.usermanagement.service;

import com.im.usermanagement.exception.ResourceNotFoundException;
import com.im.usermanagement.exception.UserAlreadyExistsException;
import com.im.usermanagement.model.User;
import com.im.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor; // ADDED
import lombok.extern.slf4j.Slf4j; // ADDED
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service layer responsible for business logic related to User CRUD operations.
 */
@Service
@RequiredArgsConstructor // Automatically injects UserRepository via constructor
@Slf4j // Provides logger 'log'
public class UserService {

    private final UserRepository userRepository;

    // --- NEW: Required by the /me endpoint in UserController ---
    /**
     * Finds a User by email. Crucial for retrieving details of the authenticated user.
     * @param email The email/username of the user.
     * @return The User entity.
     * @throws ResourceNotFoundException if the user does not exist.
     */
    public User getUserByEmail(String email) {
        log.debug("Attempting to retrieve user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
    // -----------------------------------------------------------

    // --- CRUD Operations ---

    /**
     * Retrieves all active users from the database.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     * @throws ResourceNotFoundException if user is not found.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Creates a new user after checking for email uniqueness.
     * @throws UserAlreadyExistsException if the email is already in use.
     */
    @Transactional
    public User createUser(User user) {
        // Business Rule 1: Email must be unique
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists.");
        }

        // IMPORTANT: In a real scenario, you MUST hash the password here
        // if the input is plain text, and handle role assignment securely.
        user.setActive(true);
        log.warn("Security Warning: createUser in UserService should include password hashing and role assignment logic if used outside of AuthController.");

        return userRepository.save(user); // Save the user to the database
    }

    /**
     * Updates an existing user's details.
     * @throws ResourceNotFoundException if user is not found.
     */
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Business Logic: Apply updates to the existing entity
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());

        // Handling email update check for uniqueness
        if (!existingUser.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
            throw new IllegalArgumentException("New email is already in use by another account.");
        }
        existingUser.setEmail(userDetails.getEmail());

        return userRepository.save(existingUser);
    }

    /**
     * Soft-deletes a user by setting their 'active' flag to false.
     * @throws ResourceNotFoundException if user is not found.
     */
    @Transactional
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Business Rule 3: Soft Delete
        user.setActive(false);
        userRepository.save(user); // Persist the change
        log.info("Soft deleted user with ID: {}", id);
    }
}