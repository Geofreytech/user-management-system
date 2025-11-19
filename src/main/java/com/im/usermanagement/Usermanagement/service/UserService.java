package com.im.usermanagement.service;

import com.im.usermanagement.exception.UserAlreadyExistsException; // We will create this later
import com.im.usermanagement.model.User;
import com.im.usermanagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import for transaction management


import com.im.usermanagement.exception.ResourceNotFoundException; // <-- ADD THIS LINE
// ... other imports
import com.im.usermanagement.exception.UserAlreadyExistsException; // <-- ADD THIS LINE (for createUser method)

// ... rest of the UserService code

import java.util.List;

@Service // 1. Marks this class as a Spring business service
public class UserService {

    private final UserRepository userRepository; // 2. Final field for dependency injection

    // 3. Constructor Injection: The preferred way to inject dependencies
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- CRUD Operations ---

    /**
     * Retrieves all active users from the database.
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Retrieves a user by their ID.
     * @throws com.im.usermanagement.exception.ResourceNotFoundException if user is not found.
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new com.im.usermanagement.exception.ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Creates a new user after checking for email uniqueness.
     * @throws UserAlreadyExistsException if the email is already in use.
     */
    @Transactional // 4. Ensures the entire method runs as a single database transaction
    public User createUser(User user) {
        // Business Rule 1: Email must be unique
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists.");
        }

        // Business Rule 2: You could add logic here to sanitize input, encrypt password (later), etc.

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
        // Note: We don't change the ID
        existingUser.setFirstName(userDetails.getFirstName());
        existingUser.setLastName(userDetails.getLastName());

        // Note on email update: Handling email updates requires extra checks (like uniqueness)
        // and is often more complex, so we omit it for simplicity here.

        // The save method handles both insert and update.
        // Because 'existingUser' is managed by JPA in the transaction,
        // calling save is technically optional here, but good practice.
        return userRepository.save(existingUser);
    }

    /**
     * Soft-deletes a user by setting their 'isActive' flag to false.
     * @throws ResourceNotFoundException if user is not found.
     */
    @Transactional
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Business Rule 3: Soft Delete (preferred over permanent delete in I&M bank systems)
        user.setActive(false);
        userRepository.save(user); // Persist the change
    }
}