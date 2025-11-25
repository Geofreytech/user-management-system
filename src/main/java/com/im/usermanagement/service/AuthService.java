package com.im.usermanagement.service;

import com.im.usermanagement.security.dto.RegisterRequestDTO;
// CORRECT IMPORT: Using your existing DTO name
import com.im.usermanagement.security.dto.LoginRequestDTO;
// Using the DTO name based on your file structure (AuthResponseDTO or JwtAuthResponse)
import com.im.usermanagement.security.dto.JwtAuthResponse;
import com.im.usermanagement.security.jwt.JwtTokenProvider;
import com.im.usermanagement.exception.UserAlreadyExistsException;
import com.im.usermanagement.model.Role;
import com.im.usermanagement.model.RoleName;
import com.im.usermanagement.model.User;
import com.im.usermanagement.repository.RoleRepository;
import com.im.usermanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service class handling core authentication business logic (Registration and Login).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    // --- Core Dependencies ---
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // --- Dependencies Added for Login/JWT/Notification ---
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    /**
     * Helper method to look up a Role entity by its name.
     */
    private Role getRoleByName(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> {
                    log.error("Critical Error: Role {} not found in the database.", roleName);
                    return new RuntimeException("Error: Required role not found.");
                });
    }

    // --- LOGIN METHOD WITH EMAIL NOTIFICATION ---
    /**
     * Handles the login process, authenticates the user, generates a JWT,
     * and sends a security notification email upon success.
     * * @param loginRequestDTO The DTO containing the user's email and password.
     * @return JwtAuthResponse containing the generated JWT.
     */
    public JwtAuthResponse login(LoginRequestDTO loginRequestDTO) { // FIX 1: Changed method parameter type
        log.info("Attempting login for user: {}", loginRequestDTO.getEmail()); // FIX 2: Changed variable name

        // 1. Authenticate the user using the AuthenticationManager.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()) // FIX 3: Changed variable name
        );

        // 2. Set the authenticated user in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate Token
        String token = jwtTokenProvider.generateToken(authentication);

        // 4. Retrieve the full User object to get the name for the email
        User user = userRepository.findByEmail(loginRequestDTO.getEmail()) // FIX 4: Changed variable name
                .orElseThrow(() -> new UsernameNotFoundException("User not found after successful authentication."));

        // 5. SECURITY ENHANCEMENT: Send Login Notification
        log.info("Login successful. Triggering email notification to {}.", user.getEmail());
        emailService.sendLoginNotification(user);

        // 6. Return response
        return JwtAuthResponse.builder()
                .accessToken(token)
                .build();
    }
    // --- END NEW LOGIN METHOD ---


    // --- REGISTRATION METHODS (EXISTING CODE) ---

    /**
     * Handles the secure registration of a new user for the public '/auth/register' endpoint.
     */
    public User register(RegisterRequestDTO request) {
        return register(request, false);
    }

    /**
     * Handles the registration of a new user, conditionally allowing custom role assignment.
     */
    public User register(RegisterRequestDTO request, boolean isAdminContext) {
        log.info("Attempting to register new user: {}", request.getEmail());

        // 1. Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: User with email {} already exists.", request.getEmail());
            throw new UserAlreadyExistsException("Email is already taken!");
        }

        // 2. Determine the role to assign
        Role assignedRole;

        if (isAdminContext && request.getRole() != null && request.getRole().equalsIgnoreCase(RoleName.ROLE_ADMIN.name())) {
            assignedRole = getRoleByName(RoleName.ROLE_ADMIN);
        } else {
            assignedRole = getRoleByName(RoleName.ROLE_USER);
        }
        log.info("Assigning role: {} to new user {}.", assignedRole.getName(), request.getEmail());

        // 3. Create the User entity
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .active(true)
                .roles(Collections.singletonList(assignedRole))
                .build();

        // 4. Save the new user to the database
        User savedUser = userRepository.save(user);
        log.info("Successfully registered and saved new user with ID: {}", savedUser.getId());

        return savedUser;
    }
}