package com.products.demo.Controller;
import com.products.demo.Model.User;
import com.products.demo.Service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Updated import
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        logger.info("Fetching all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        logger.info("Fetching user with ID: {}", id);
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            logger.warn("User with ID: {} not found", id);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        logger.info("Creating new user with email: {}", user.getEmail());
        try {
            return userService.createUser(user);
        } catch (Exception e) {
            logger.error("Failed to create user with email: {}. Error: {}", user.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        logger.info("Updating user with ID: {}", id);
        User updatedUser = userService.updateUser(id, userDetails);
        if (updatedUser != null) {
            return ResponseEntity.ok(updatedUser);
        } else {
            logger.warn("User with ID: {} not found for update", id);
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        logger.info("Deleting user with ID: {}", id);
        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            logger.debug("User with ID: {} deleted successfully", id);
            return ResponseEntity.ok().build();
        } else {
            logger.warn("User with ID: {} not found for deletion", id);
            return ResponseEntity.notFound().build();
        }
    }
}