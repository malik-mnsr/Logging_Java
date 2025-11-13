package com.products.demo.Security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {
    private static final Logger logger = LogManager.getLogger(JwtUtil.class);
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 hours

    public String extractUsername(String token) {
        logger.debug("Extracting username from JWT");
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.debug("Username extracted: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Failed to extract username from JWT. Error: {}", e.getMessage());
            throw e;
        }
    }

    public Date extractExpiration(String token) {
        logger.debug("Extracting expiration from JWT");
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            logger.debug("Expiration extracted: {}", expiration);
            return expiration;
        } catch (Exception e) {
            logger.error("Failed to extract expiration from JWT. Error: {}", e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        logger.debug("Extracting claim from JWT");
        try {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        } catch (Exception e) {
            logger.error("Failed to extract claim from JWT. Error: {}", e.getMessage());
            throw e;
        }
    }

    private Claims extractAllClaims(String token) {
        logger.debug("Parsing all claims from JWT");
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            logger.error("Invalid JWT token. Error: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        logger.debug("Checking if JWT is expired");
        try {
            boolean expired = extractExpiration(token).before(new Date());
            logger.debug("JWT expired: {}", expired);
            return expired;
        } catch (Exception e) {
            logger.error("Failed to check JWT expiration. Error: {}", e.getMessage());
            throw e;
        }
    }

    public String generateToken(String username) {
        logger.info("Generating JWT for username: {}", username);
        try {
            Map<String, Object> claims = new HashMap<>();
            String token = createToken(claims, username);
            logger.debug("JWT generated successfully for username: {}", username);
            return token;
        } catch (Exception e) {
            logger.error("Failed to generate JWT for username: {}. Error: {}", username, e.getMessage());
            throw e;
        }
    }

    private String createToken(Map<String, Object> claims, String subject) {
        logger.debug("Creating JWT with subject: {}", subject);
        try {
            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .signWith(SECRET_KEY)
                    .compact();
        } catch (Exception e) {
            logger.error("Failed to create JWT for subject: {}. Error: {}", subject, e.getMessage());
            throw e;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        logger.info("Validating JWT for user: {}", userDetails.getUsername());
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            logger.debug("JWT validation result for user {}: {}", userDetails.getUsername(), isValid);
            return isValid;
        } catch (Exception e) {
            logger.error("Failed to validate JWT for user: {}. Error: {}", userDetails.getUsername(), e.getMessage());
            throw e;
        }
    }
}