package com.im.usermanagement.Usermanagement.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Main configuration class for Spring Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Defines the security filter chain (authorization rules).
     * This is the core of how Spring Security handles requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Since this is a REST API, disable CSRF protection
                .csrf(csrf -> csrf.disable())

                // Configure request authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to access the registration and public endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Require ADMIN role for endpoints under /api/admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // All other requests must be authenticated (logged in)
                        .anyRequest().authenticated()
                )

                // Configure session management to be stateless (essential for JWT/REST)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Note: Token and Authentication filter definitions go here later

                // Configure basic form login (will be replaced by JWT later, but good for testing)
                .httpBasic(httpBasic -> httpBasic.init(http));

        return http.build();
    }

    /**
     * Defines the standard password encoder (BCrypt is recommended and widely used).
     * This bean is used by the UserService during user creation and by
     * the AuthenticationManager during login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager bean, which is required for explicit authentication
     * (e.g., in a login controller).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}