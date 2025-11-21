package com.im.usermanagement.security.config;

import com.im.usermanagement.security.jwt.JwtAuthEntryPoint;
import com.im.usermanagement.security.jwt.JwtAuthenticationFilter;
import com.im.usermanagement.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Spring Security Configuration Class.
 * Configures global security settings, including JWT filtering and public endpoint access.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthEntryPoint jwtAuthEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Defines the PasswordEncoder bean. Uses BCrypt for secure password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the AuthenticationManager bean, which is required for the login process.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Configures the DaoAuthenticationProvider which uses the custom UserDetailsService
     * and the configured PasswordEncoder.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Configures the main Security Filter Chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF (Stateless API doesn't need it)
                .csrf(csrf -> csrf.disable())

                // 2. Configure Exception Handling for unauthorized access (401)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthEntryPoint)
                )

                // 3. Set session management to stateless (Crucial for JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Configure Authorization Rules
                .authorizeHttpRequests(authorize -> authorize
                        // FIX: Added /api/v1/auth/** to permit access to the controller
                        .requestMatchers(
                                "/api/v1/auth/**",      // Permit all v1 auth endpoints (register/login)
                                "/h2-console/**",       // Permit H2 console
                                "/v3/api-docs/**",      // Permit Swagger UI documentation
                                "/swagger-ui/**",
                                "/webjars/**"
                        ).permitAll()

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )

                // 5. IMPORTANT: Fix for H2 Console to display properly in the browser (disables X-Frame-Options)
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        // 6. Add the custom JWT filter before the standard Spring Security filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 7. Apply the Authentication Provider
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }
}