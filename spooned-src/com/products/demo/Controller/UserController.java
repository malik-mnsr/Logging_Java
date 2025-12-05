package com.products.demo.Controller;
import com.products.demo.Model.User;
import com.products.demo.Service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "READ", "/api/users", null);;
        logger.info("Fetching all users");
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable
    Long id) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "READ", "/api/users/{id}", (java.lang.Long)id);;
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
    public User createUser(@Valid
    @RequestBody
    User user) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/users", null);;
        logger.info("Creating new user with email: {}", user.getEmail());
        try {
            return userService.createUser(user);
        } catch (Exception e) {
            logger.error("Failed to create user with email: {}. Error: {}", user.getEmail(), e.getMessage());
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable
    Long id, @Valid
    @RequestBody
    User userDetails) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/users/{id}", (java.lang.Long)id);;
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
    public ResponseEntity<?> deleteUser(@PathVariable
    Long id) {
        com.products.demo.spoon.UserProfileLogger.logRequest(org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication(), "WRITE", "/api/users/{id}", (java.lang.Long)id);;
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