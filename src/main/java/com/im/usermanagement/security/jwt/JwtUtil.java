package com.im.usermanagement.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    // 1. JWT Secret Key (Should be stored securely, e.g., in application.properties)
    @Value("${jwt.secret}")
    private String secret;

    // 2. Token expiration time (1 hour)
    @Value("${jwt.expiration}")
    private long expiration;

    // --- Token Generation ---
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // The username
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret) // Use HS256 algorithm and the secret key
                .compact();
    }

    // --- Token Validation/Extraction methods (to be added later) ---
}