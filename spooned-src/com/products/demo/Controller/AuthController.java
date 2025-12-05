package com.products.demo.Controller;
import com.products.demo.Dto.AuthRequest;
import com.products.demo.Dto.AuthResponse;
import com.products.demo.Model.User;
import com.products.demo.Service.AuthService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid
    @RequestBody
    User user) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/auth/register", null);;
        logger.info("Register request received for email: {}", user.getEmail());
        try {
            AuthResponse response = authService.register(user);
            logger.debug("Registration successful for email: {}", user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Registration failed for email: {}. Error: {}", user.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid
    @RequestBody
    AuthRequest authRequest) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/auth/login", null);;
        logger.info("Login request received for email: {}", authRequest.getEmail());
        try {
            AuthResponse response = authService.authenticate(authRequest);
            logger.debug("Login successful for email: {}", authRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Login failed for email: {}. Error: {}", authRequest.getEmail(), e.getMessage());
            throw e;
        }
    }
}