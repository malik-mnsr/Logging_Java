package com.products.demo.Service;
import com.products.demo.Model.User;
import com.products.demo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        logger.info("Fetching all users from repository");
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            logger.warn("User with ID: {} not found", id);
        }
        return user;
    }

    public User createUser(User user) {
        logger.info("Creating user with email: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        logger.debug("User created: {}", savedUser.getEmail());
        return savedUser;
    }

    public User updateUser(Long id, User userDetails) {
        logger.info("Updating user with ID: {}", id);
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(userDetails.getName());
            user.setEmail(userDetails.getEmail());
            user.setAge(userDetails.getAge());
            if ((userDetails.getPassword() != null) && (!userDetails.getPassword().isEmpty())) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }
            User updatedUser = userRepository.save(user);
            logger.debug("User updated: {}", updatedUser.getEmail());
            return updatedUser;
        }
        logger.warn("User with ID: {} not found for update", id);
        return null;
    }

    public boolean deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            logger.debug("User with ID: {} deleted", id);
            return true;
        }
        logger.warn("User with ID: {} not found for deletion", id);
        return false;
    }

    public Optional<User> getUserByEmail(String email) {
        logger.info("Fetching user with email: {}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (!user.isPresent()) {
            logger.warn("User with email: {} not found", email);
        }
        return user;
    }
}