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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Custom filter that executes once per request to validate JWTs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * List of paths that should bypass this JWT filter entirely.
     * These paths MUST match the .permitAll() paths in SecurityConfig.
     */
    private static final List<RequestMatcher> PUBLIC_PATHS = Arrays.asList(
            // General common root requests
            new AntPathRequestMatcher("/"),             // Root path
            new AntPathRequestMatcher("/favicon.ico"),  // Browser icon request

            // Auth endpoints
            new AntPathRequestMatcher("/api/v1/auth/**"),

            // Swagger/OpenAPI documentation
            new AntPathRequestMatcher("/v3/api-docs/**"),
            new AntPathRequestMatcher("/swagger-ui/**"),
            new AntPathRequestMatcher("/webjars/**"),
            new AntPathRequestMatcher("/swagger-ui.html"),

            // H2 Console
            new AntPathRequestMatcher("/h2-console/**")
    );


    /**
     * Overrides the default method to implement conditional filtering.
     * Returns true if the filter should NOT run (i.e., for public paths).
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Check if the current request URI matches any of the defined public paths
        return PUBLIC_PATHS.stream().anyMatch(matcher -> matcher.matches(request));
    }


    /**
     * Main filter logic for authenticated requests.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // Get the username (email) from the token, as defined in JwtTokenProvider
                String username = tokenProvider.getUsernameFromJWT(jwt);

                // Load user details by username (not ID)
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                // Create authentication object
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set authentication in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Log the exception but do not halt the chain here,
            // the JwtAuthEntryPoint will handle the ultimate rejection if no auth is set.
            log.warn("Authentication failed for request to protected resource: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from the Authorization header of the request.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if the header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Return the token part (everything after "Bearer ")
            return bearerToken.substring(7);
        }
        return null;
    }
}