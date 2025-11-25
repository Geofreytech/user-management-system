package com.im.usermanagement.security.config;

import com.im.usermanagement.security.jwt.JwtAuthEntryPoint;
import com.im.usermanagement.security.jwt.JwtAuthenticationFilter;
import com.im.usermanagement.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
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
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

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
     * This bean is necessary to resolve the "AuthenticationManager bean could not be found" error.
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
     * Creates a bean for MvcRequestMatcher.Builder, often necessary for using
     * requestMatchers in a Spring MVC context to correctly handle path variables.
     * @param introspector The HandlerMappingIntrospector used by Spring MVC.
     * @return A builder for creating MvcRequestMatchers.
     */
    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

    /**
     * Configures the main Security Filter Chain.
     * @param http The HttpSecurity object to configure.
     * @param mvc The MvcRequestMatcher.Builder for advanced request matching.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
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

                // 4. Configure Authorization Rules (RBAC)
                .authorizeHttpRequests(authorize -> authorize
                        // Allow access to common static resources (CSS, JS, images, etc.)
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // Public Endpoints (Permit All)
                        .requestMatchers(
                                mvc.pattern("/api/v1/auth/**"),      // Permit all v1 auth endpoints (register/login)
                                mvc.pattern("/h2-console/**"),       // Permit H2 console
                                mvc.pattern("/v3/api-docs/**"),      // Permit Swagger UI documentation
                                mvc.pattern("/swagger-ui/**"),
                                mvc.pattern("/webjars/**")
                        ).permitAll()

                        // RBAC: Rule 1 - Allow any authenticated user to access their own profile
                        .requestMatchers(mvc.pattern("/api/v1/users/me")).authenticated()

                        // RBAC: Rule 2 - Restrict all other user management paths to ADMIN role
                        .requestMatchers(mvc.pattern("/api/v1/users/**")).hasRole("ADMIN")

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