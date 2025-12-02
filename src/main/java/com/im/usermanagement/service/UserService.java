package com.im.usermanagement.service;

import com.im.usermanagement.exception.ResourceNotFoundException;
import com.im.usermanagement.exception.UserAlreadyExistsException;
import com.im.usermanagement.model.User;
import com.im.usermanagement.model.Role;
import com.im.usermanagement.repository.UserRepository;
import com.im.usermanagement.security.dto.UserProfileResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

/**
 * Service layer responsible for business logic related to User CRUD operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
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

    /**
     * Retrieves the profile details for the currently authenticated user.
     * @return UserProfileResponseDTO containing non-sensitive user data.
     * @throws RuntimeException if the user is not found in the database.
     */
    public UserProfileResponseDTO getCurrentUserProfile() {
        // 1. Get the authenticated principal (the user details object)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // The principal is typically the email or username passed into the JWT payload (the 'sub' claim)
        String userIdentifier = authentication.getName();

        // 2. Find the user in the database
        // Assuming your repository has a method to find by the principal's name (which is often the email)
        Optional<User> userOptional = userRepository.findByEmail(userIdentifier);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Authenticated user not found in database: " + userIdentifier);
        }

        User user = userOptional.get();

        // 3. Convert the User entity to the safe DTO (Projection)
        return UserProfileResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername()) // Include if you use a separate username field
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                // FIX: Added .name() call to convert the RoleName enum (which is the result of getName()) to a String.
                .role(user.getRoles().stream()
                        .findFirst()
                        .map(Role::getName)
                        .map(Enum::name) // <-- THE FINAL FIX: Convert the RoleName enum to its String representation
                        .orElse(null))
                .build();
    }

}