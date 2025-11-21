package com.im.usermanagement.controller;

import com.im.usermanagement.model.Role;
import com.im.usermanagement.model.RoleName;
import com.im.usermanagement.model.User;
import com.im.usermanagement.repository.RoleRepository;
import com.im.usermanagement.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
// FIX: Added "/v1" back to the request mapping to match SecurityConfig
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    // Inject all required dependencies
    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
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
     * @param registerDTO Contains new user details.
     * @return Success message.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequestDTO registerDTO) {

        // Check 1: Ensure email is not already in use
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            return new ResponseEntity<>("Email is already taken!", HttpStatus.BAD_REQUEST);
        }

        // 2. Create the User object
        User user = new User();
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        user.setEmail(registerDTO.getEmail());

        // HASH the password before saving!
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        // 3. Assign the default role (ROLE_USER)
        Role roles = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Role not found. Please initialize roles in DB."));

        user.setRoles(Collections.singletonList(roles));

        // 4. Save the new user
        userRepository.save(user);

        return new ResponseEntity<>("User registered successfully!", HttpStatus.CREATED);
    }
}