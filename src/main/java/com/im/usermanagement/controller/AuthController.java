package com.im.usermanagement.controller;

import com.im.usermanagement.service.AuthService; // <-- NEW: Import AuthService
import com.im.usermanagement.security.dto.AuthResponseDTO;
import com.im.usermanagement.security.dto.LoginRequestDTO;
import com.im.usermanagement.security.dto.RegisterRequestDTO;
import com.im.usermanagement.security.jwt.JwtTokenProvider;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// NOTE: Removed unused imports like Role, UserRepository, PasswordEncoder, etc.

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService; // <-- ADDED: Dependency for AuthService

    // Inject all required dependencies
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider, // <-- Retained
                          AuthService authService) { // <-- ADDED: Inject AuthService
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.authService = authService; // <-- INITIALIZED
    }

    /**
     * Endpoint for user login. Path: /api/v1/auth/login
     * @param loginDTO Contains email and password.
     * @return JWT Token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> authenticateUser(@Valid @RequestBody LoginRequestDTO loginDTO) {

        // 1. Authenticate credentials using the AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        // 2. Set the authenticated user in the security context (optional, but good practice)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Generate the JWT token
        String jwt = tokenProvider.generateToken(authentication);

        // 4. Return the JWT token to the client
        return new ResponseEntity<>(new AuthResponseDTO(jwt), HttpStatus.OK);
    }

    /**
     * Endpoint for user registration. Path: /api/v1/auth/register
     * This method delegates all registration logic (validation, hashing, role assignment) to AuthService.
     * @param registerDTO Contains new user details.
     * @return Success message.
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequestDTO registerDTO) {

        // Delegate all registration logic (user existence check, password hashing,
        // role assignment, and saving) to the AuthService.
        // The service will throw an exception (like UserAlreadyExistsException) if validation fails.
        authService.register(registerDTO);

        // If the service call succeeds without throwing an exception, return 201 Created.
        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }
}