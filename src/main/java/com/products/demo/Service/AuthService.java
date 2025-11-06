package com.products.demo.Service;

import com.products.demo.Dto.AuthRequest;
import com.products.demo.Dto.AuthResponse;
import com.products.demo.Model.User;
import com.products.demo.Security.CustomUserDetailsService;
import com.products.demo.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());

        return new AuthResponse(jwt, "Login successful");
    }

    public AuthResponse register(User user) {
        if (userService.getUserByEmail(user.getEmail()).isPresent()) {
            return new AuthResponse(null, "Email already exists");
        }

        User savedUser = userService.createUser(user);
        final String jwt = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(jwt, "Registration successful");
    }
}