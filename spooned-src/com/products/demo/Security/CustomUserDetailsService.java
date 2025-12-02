package com.products.demo.Security;
import com.products.demo.Model.User;
import com.products.demo.repository.UserRepository;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LogManager.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.info("Loading user details for email: {}", email);
        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> {
                logger.warn("User not found with email: {}", email);
                return new UsernameNotFoundException("User not found with email: " + email);
            });
            logger.debug("User details loaded successfully for email: {}", email);
            return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), Collections.emptyList());
        } catch (UsernameNotFoundException e) {
            throw e;// Exception already logged

        } catch (Exception e) {
            logger.error("Error loading user details for email: {}. Error: {}", email, e.getMessage());
            throw e;
        }
    }
}