package com.im.usermanagement.service;

import com.im.usermanagement.dto.RegisterRequest;
import com.im.usermanagement.exception.UserAlreadyExistsException;
import com.im.usermanagement.model.Role;
import com.im.usermanagement.model.RoleName;
import com.im.usermanagement.model.User;
import com.im.usermanagement.repository.RoleRepository;
import com.im.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service class handling core authentication business logic, such as registration.
 * This class injects the PasswordEncoder to ensure passwords are saved securely (hashed).
 */
@Service
@RequiredArgsConstructor // Lombok generates a constructor for all final fields (Dependency Injection)
@Slf4j
public class AuthService {

    // Inject all required dependencies: Repositories and the PasswordEncoder
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Handles the secure registration of a new user.
     * @param request The DTO containing the user's registration details.
     * @return The newly created User entity.
     * @throws UserAlreadyExistsException if the email is already in use.
     */
    public User register(RegisterRequest request) {
        log.info("Attempting to register new user: {}", request.getEmail());

        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: User with email {} already exists.", request.getEmail());
            throw new UserAlreadyExistsException("Email is already taken!");
        }

        // 2. Look up the default user role (ROLE_USER)
        Role defaultRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> {
                    // This error indicates a critical setup issue (DataLoader failed)
                    log.error("Critical Error: Default role ROLE_USER not found in the database.");
                    return new RuntimeException("Error: Default role not found. Please ensure roles are initialized.");
                });

        // 3. Create the User entity
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                // CRITICAL: HASH the password using the injected PasswordEncoder
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true) // Ensure the account is active
                .roles(Collections.singletonList(defaultRole)) // Assign the default role
                .build();

        // 4. Save the new user to the database
        User savedUser = userRepository.save(user);
        log.info("Successfully registered and saved new user with ID: {}", savedUser.getId());

        return savedUser;
    }
}