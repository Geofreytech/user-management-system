package com.im.usermanagement.security.jwt;

import com.im.usermanagement.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Custom filter that executes once per request to check for a valid JWT in the Authorization header.
 * If a valid token is found, it loads the user and sets the authentication context for the request.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Inject our utility classes
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Extracts the JWT from the Authorization header (format: "Bearer <token>").
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if the token is present and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Return the token part after "Bearer "
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Get the JWT from the request
            String jwt = getJwtFromRequest(request);

            // 2. Validate the token and authenticate the user
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // Get username from token
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // Load user data associated with the token (e.g., roles/authorities)
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Construct Authentication token required by Spring Security
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // credentials field is null for token-based auth
                        userDetails.getAuthorities()
                );

                // Set additional details like the request IP address
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the Authentication object in the SecurityContext, completing the authentication process
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // In a real application, you might want more specific handling here,
            // but for now, we just log and let the request proceed.
            log.error("Could not set user authentication in security context", ex);
        }

        // 3. Pass the request to the next filter in the chain (or the final endpoint)
        filterChain.doFilter(request, response);
    }
}