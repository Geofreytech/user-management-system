package com.im.usermanagement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. PasswordEncoder Bean (REQUIRED for hashing)
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt is the industry standard for secure password hashing
        return new BCryptPasswordEncoder();
    }

    // 2. AuthenticationManager Bean (REQUIRED by AuthController)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // *** IMPORTANT FIX: The temporary 'userDetailsService()' bean was removed. ***
    // *** Spring now automatically uses the @Service annotated CustomUserDetailsService. ***

    // 3. Security Filter Chain (The main configuration)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless REST APIs (using modern syntax)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Make the API stateless
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll() // Allow login and registration
                        .requestMatchers("/h2-console/**").permitAll() // Allow H2 console
                        .anyRequest().authenticated() // Secure everything else
                );

        // Security headers configuration (for H2 console compatibility)
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}