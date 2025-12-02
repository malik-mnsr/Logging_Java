package com.products.demo.Service;
import com.products.demo.Dto.AuthRequest;
import com.products.demo.Dto.AuthResponse;
import com.products.demo.Model.User;
import com.products.demo.Security.CustomUserDetailsService;
import com.products.demo.Security.JwtUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
    private static final Logger logger = LogManager.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse authenticate(AuthRequest authRequest) {
        logger.info("Authenticating user with email: {}", authRequest.getEmail());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());
            logger.debug("JWT generated for user: {}", authRequest.getEmail());
            return new AuthResponse(jwt, "Login successful");
        } catch (Exception e) {
            logger.error("Authentication failed for email: {}. Error: {}", authRequest.getEmail(), e.getMessage());
            throw e;
        }
    }

    public AuthResponse register(User user) {
        logger.info("Registering new user with email: {}", user.getEmail());
        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email {} already exists", user.getEmail());
            return new AuthResponse(null, "Email already exists");
        }
        try {
            User savedUser = userService.createUser(user);
            final String jwt = jwtUtil.generateToken(savedUser.getEmail());
            logger.debug("User registered and JWT generated for email: {}", savedUser.getEmail());
            return new AuthResponse(jwt, "Registration successful");
        } catch (Exception e) {
            logger.error("Registration failed for email: {}. Error: {}", user.getEmail(), e.getMessage());
            throw e;
        }
    }
}