package com.im.usermanagement.Usermanagement.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom implementation for handling unauthorized access attempts.
 * This class is invoked when a user tries to access a secured endpoint without
 * valid credentials (e.g., missing or invalid JWT).
 */
@Component
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Unauthorized error: {}", authException.getMessage());

        // Respond with HTTP 401 Unauthorized status and a message
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized. Access Denied.");
    }
}